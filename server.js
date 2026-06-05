const fs = require('fs');
const http = require('http');
const path = require('path');

const HOST = '127.0.0.1';
const PORT = Number(process.env.PORT || 8080);
const ROOT_DIR = __dirname;

const MIME_TYPES = {
  '.css': 'text/css; charset=utf-8',
  '.html': 'text/html; charset=utf-8',
  '.js': 'text/javascript; charset=utf-8',
  '.json': 'application/json; charset=utf-8',
  '.png': 'image/png',
  '.svg': 'image/svg+xml; charset=utf-8',
};

const INITIAL_STUDENTS = [
  { id: 1000, name: 'Alice Johnson', subject1: 95, subject2: 88, subject3: 92, subject4: 90 },
  { id: 1001, name: 'Bob Smith', subject1: 72, subject2: 65, subject3: 70, subject4: 68 },
  { id: 1002, name: 'Charlie Brown', subject1: 55, subject2: 60, subject3: 58, subject4: 62 },
  { id: 1003, name: 'Diana Prince', subject1: 45, subject2: 40, subject3: 38, subject4: 42 },
  { id: 1004, name: 'Ethan Hunt', subject1: 82, subject2: 78, subject3: 85, subject4: 80 },
];

let students = INITIAL_STUDENTS.map(buildStudent);
let nextId = 1005;

function roundToTwo(value) {
  return Math.round(value * 100) / 100;
}

function calculateGrade(average) {
  if (average >= 90) return { grade: 'A', performanceMessage: 'Excellent' };
  if (average >= 75) return { grade: 'B', performanceMessage: 'Very Good' };
  if (average >= 60) return { grade: 'C', performanceMessage: 'Good' };
  if (average >= 50) return { grade: 'D', performanceMessage: 'Needs Improvement' };
  return { grade: 'F', performanceMessage: 'Failed' };
}

function buildStudent(data) {
  const average = roundToTwo((data.subject1 + data.subject2 + data.subject3 + data.subject4) / 4);
  const gradeInfo = calculateGrade(average);

  return {
    id: data.id,
    name: data.name,
    subject1: data.subject1,
    subject2: data.subject2,
    subject3: data.subject3,
    subject4: data.subject4,
    average,
    grade: gradeInfo.grade,
    performanceMessage: gradeInfo.performanceMessage,
  };
}

function parseCookies(header = '') {
  return header.split(';').reduce((cookies, item) => {
    const separatorIndex = item.indexOf('=');
    if (separatorIndex === -1) return cookies;

    const key = item.slice(0, separatorIndex).trim();
    const value = item.slice(separatorIndex + 1).trim();

    if (key) {
      cookies[key] = decodeURIComponent(value);
    }

    return cookies;
  }, {});
}

function isLoggedIn(req) {
  return parseCookies(req.headers.cookie).loggedInUser === 'admin';
}

function sendJson(res, statusCode, payload, headers = {}) {
  res.writeHead(statusCode, {
    'Content-Type': 'application/json; charset=utf-8',
    ...headers,
  });
  res.end(JSON.stringify(payload));
}

function sendRedirect(res, location) {
  res.writeHead(302, { Location: location });
  res.end();
}

function readRequestJson(req) {
  return new Promise((resolve, reject) => {
    let body = '';

    req.on('data', (chunk) => {
      body += chunk;

      if (body.length > 1_000_000) {
        reject(new Error('Request body is too large.'));
        req.destroy();
      }
    });

    req.on('end', () => {
      try {
        resolve(body ? JSON.parse(body) : {});
      } catch {
        reject(new Error('Malformed JSON input'));
      }
    });

    req.on('error', reject);
  });
}

function validateStudentInput(data) {
  const name = typeof data.name === 'string' ? data.name.trim() : '';
  const scores = ['subject1', 'subject2', 'subject3', 'subject4'].map((key) => Number(data[key]));

  if (!name) {
    return { error: 'Student Name cannot be empty.' };
  }

  if (scores.some(Number.isNaN)) {
    return { error: 'All marks must be valid numbers.' };
  }

  if (scores.some((score) => score < 0 || score > 100)) {
    return { error: 'Marks must be between 0 and 100.' };
  }

  return {
    student: {
      id: nextId++,
      name,
      subject1: scores[0],
      subject2: scores[1],
      subject3: scores[2],
      subject4: scores[3],
    },
  };
}

function getDashboardMetrics() {
  const total = students.length;
  const gradePriority = { A: 5, B: 4, C: 3, D: 2, F: 1 };
  const counts = { A: 0, B: 0, C: 0, D: 0, F: 0 };

  for (const student of students) {
    counts[student.grade] += 1;
  }

  const classAverage = total
    ? roundToTwo(students.reduce((sum, student) => sum + student.average, 0) / total)
    : 0;

  const topGrade = total
    ? students.reduce((top, student) => (
      gradePriority[student.grade] > gradePriority[top] ? student.grade : top
    ), 'F')
    : 'N/A';

  return {
    totalStudents: total,
    classAverage,
    topGrade,
    gradeA: counts.A,
    gradeB: counts.B,
    gradeC: counts.C,
    gradeD: counts.D,
    gradeF: counts.F,
  };
}

async function handleLogin(req, res, searchParams) {
  if (req.method === 'GET') {
    if (searchParams.get('action') === 'logout') {
      return sendJson(res, 200, { status: 'success', message: 'Logged out successfully' }, {
        'Set-Cookie': 'loggedInUser=; Path=/; Max-Age=0; SameSite=Lax',
      });
    }

    const loggedIn = isLoggedIn(req);
    return sendJson(res, 200, {
      status: 'success',
      loggedIn,
      user: loggedIn ? 'admin' : null,
    });
  }

  if (req.method !== 'POST') {
    return sendJson(res, 405, { status: 'error', message: 'Method not allowed.' });
  }

  try {
    const credentials = await readRequestJson(req);

    if (credentials.username === 'admin' && credentials.password === 'admin123') {
      return sendJson(res, 200, { status: 'success', message: 'Login successful' }, {
        'Set-Cookie': 'loggedInUser=admin; Path=/; HttpOnly; Max-Age=1800; SameSite=Lax',
      });
    }

    return sendJson(res, 401, { status: 'error', message: 'Invalid username or password.' });
  } catch (error) {
    return sendJson(res, 400, { status: 'error', message: error.message });
  }
}

async function handleStudent(req, res, searchParams) {
  if (req.method === 'POST') {
    try {
      const input = await readRequestJson(req);
      const validation = validateStudentInput(input);

      if (validation.error) {
        return sendJson(res, 400, { status: 'error', message: validation.error });
      }

      const student = buildStudent(validation.student);
      students.push(student);

      return sendJson(res, 200, {
        status: 'success',
        message: 'Student added successfully',
        data: student,
      });
    } catch (error) {
      return sendJson(res, 400, { status: 'error', message: error.message });
    }
  }

  if (req.method === 'DELETE') {
    const id = Number(searchParams.get('id'));

    if (Number.isNaN(id)) {
      return sendJson(res, 400, { status: 'error', message: 'Student ID must be numeric.' });
    }

    const originalLength = students.length;
    students = students.filter((student) => student.id !== id);

    if (students.length === originalLength) {
      return sendJson(res, 404, { status: 'error', message: `Student with ID ${id} not found.` });
    }

    return sendJson(res, 200, { status: 'success', message: 'Student deleted successfully' });
  }

  return sendJson(res, 405, { status: 'error', message: 'Method not allowed.' });
}

function serveStaticFile(res, pathname) {
  const requestedPath = pathname === '/' ? '/index.html' : pathname;
  const fullPath = path.normalize(path.join(ROOT_DIR, requestedPath));

  if (!fullPath.startsWith(ROOT_DIR)) {
    res.writeHead(403, { 'Content-Type': 'text/plain; charset=utf-8' });
    return res.end('Forbidden');
  }

  fs.readFile(fullPath, (error, content) => {
    if (error) {
      const statusCode = error.code === 'ENOENT' ? 404 : 500;
      const message = statusCode === 404 ? '404 File Not Found' : `Server Error: ${error.code}`;
      res.writeHead(statusCode, { 'Content-Type': 'text/plain; charset=utf-8' });
      return res.end(message);
    }

    const contentType = MIME_TYPES[path.extname(fullPath).toLowerCase()] || 'application/octet-stream';
    res.writeHead(200, { 'Content-Type': contentType });
    res.end(content);
  });
}

async function handleRequest(req, res) {
  const requestUrl = new URL(req.url, `http://${req.headers.host || `${HOST}:${PORT}`}`);
  const pathname = decodeURIComponent(requestUrl.pathname);
  const loggedIn = isLoggedIn(req);

  const isPublicRoute = pathname === '/login.html'
    || pathname === '/login'
    || pathname.startsWith('/css/')
    || pathname.startsWith('/js/');

  if (!isPublicRoute && !loggedIn) {
    const isApiRoute = pathname === '/student' || pathname === '/students' || pathname === '/dashboard';
    return isApiRoute
      ? sendJson(res, 401, { status: 'error', message: 'Unauthorized. Please login.' })
      : sendRedirect(res, '/login.html');
  }

  if (pathname === '/login') {
    return handleLogin(req, res, requestUrl.searchParams);
  }

  if (pathname === '/students' && req.method === 'GET') {
    return sendJson(res, 200, students);
  }

  if (pathname === '/dashboard' && req.method === 'GET') {
    return sendJson(res, 200, getDashboardMetrics());
  }

  if (pathname === '/student') {
    return handleStudent(req, res, requestUrl.searchParams);
  }

  return serveStaticFile(res, pathname);
}

const server = http.createServer((req, res) => {
  handleRequest(req, res).catch((error) => {
    sendJson(res, 500, { status: 'error', message: error.message || 'Internal server error.' });
  });
});

server.listen(PORT, HOST, () => {
  console.log('==================================================');
  console.log(`SGS Local Dev Server started on port ${PORT}`);
  console.log(`Local URL: http://${HOST}:${PORT}/`);
  console.log('==================================================');
});

process.on('SIGINT', () => {
  server.close(() => {
    console.log('\nServer stopped.');
    process.exit(0);
  });
});
