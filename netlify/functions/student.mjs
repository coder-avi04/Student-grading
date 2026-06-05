import { getDatabase } from '@netlify/database';

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

export default async (req, context) => {
  if (context.cookies.get('loggedInUser') !== 'admin') {
    return Response.json({ status: 'error', message: 'Unauthorized. Please login.' }, { status: 401 });
  }

  const db = getDatabase();
  const url = new URL(req.url);

  if (req.method === 'POST') {
    let data;
    try {
      data = await req.json();
    } catch {
      return Response.json({ status: 'error', message: 'Invalid request body.' }, { status: 400 });
    }

    const name = typeof data.name === 'string' ? data.name.trim() : '';
    if (!name) {
      return Response.json({ status: 'error', message: 'Student Name cannot be empty.' }, { status: 400 });
    }

    const scores = ['subject1', 'subject2', 'subject3', 'subject4'].map(k => Number(data[k]));

    if (scores.some(s => isNaN(s))) {
      return Response.json({ status: 'error', message: 'All marks must be valid numbers.' }, { status: 400 });
    }

    if (scores.some(s => s < 0 || s > 100)) {
      return Response.json({ status: 'error', message: 'Marks must be between 0 and 100.' }, { status: 400 });
    }

    const average = roundToTwo((scores[0] + scores[1] + scores[2] + scores[3]) / 4);
    const { grade, performanceMessage } = calculateGrade(average);

    const [student] = await db.sql`
      INSERT INTO students (name, subject1, subject2, subject3, subject4, average, grade, performance_message)
      VALUES (${name}, ${scores[0]}, ${scores[1]}, ${scores[2]}, ${scores[3]}, ${average}, ${grade}, ${performanceMessage})
      RETURNING id, name, subject1, subject2, subject3, subject4, average, grade,
                performance_message AS "performanceMessage"
    `;

    return Response.json({ status: 'success', message: 'Student added successfully', data: student });
  }

  if (req.method === 'DELETE') {
    const id = Number(url.searchParams.get('id'));

    if (isNaN(id)) {
      return Response.json({ status: 'error', message: 'Student ID must be numeric.' }, { status: 400 });
    }

    const [deleted] = await db.sql`DELETE FROM students WHERE id = ${id} RETURNING id`;

    if (!deleted) {
      return Response.json({ status: 'error', message: `Student with ID ${id} not found.` }, { status: 404 });
    }

    return Response.json({ status: 'success', message: 'Student deleted successfully' });
  }

  return Response.json({ status: 'error', message: 'Method not allowed.' }, { status: 405 });
};

export const config = {
  path: '/student',
};
