export default async (req, context) => {
  const url = new URL(req.url);

  if (req.method === 'GET') {
    if (url.searchParams.get('action') === 'logout') {
      return Response.json(
        { status: 'success', message: 'Logged out successfully' },
        { headers: { 'Set-Cookie': 'loggedInUser=; Path=/; Max-Age=0; SameSite=Lax' } }
      );
    }

    const loggedIn = context.cookies.get('loggedInUser') === 'admin';
    return Response.json({ status: 'success', loggedIn, user: loggedIn ? 'admin' : null });
  }

  if (req.method === 'POST') {
    let credentials;
    try {
      credentials = await req.json();
    } catch {
      return Response.json({ status: 'error', message: 'Invalid request body.' }, { status: 400 });
    }

    const { username, password } = credentials;

    if (username === 'admin' && password === 'admin123') {
      return Response.json(
        { status: 'success', message: 'Login successful' },
        { headers: { 'Set-Cookie': 'loggedInUser=admin; Path=/; HttpOnly; Max-Age=1800; SameSite=Lax' } }
      );
    }

    return Response.json(
      { status: 'error', message: 'Invalid username or password.' },
      { status: 401 }
    );
  }

  return Response.json({ status: 'error', message: 'Method not allowed.' }, { status: 405 });
};

export const config = {
  path: '/login',
};
