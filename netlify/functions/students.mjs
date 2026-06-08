import { getDatabase } from '@netlify/database';

export default async (req, context) => {
  if (context.cookies.get('loggedInUser') !== 'admin') {
    return Response.json({ status: 'error', message: 'Unauthorized. Please login.' }, { status: 401 });
  }

  const db = getDatabase();
  const rows = await db.sql`
    SELECT id, name, subject1, subject2, subject3, subject4, average, grade,
           performance_message AS "performanceMessage"
    FROM students
    ORDER BY id
  `;

  return Response.json(rows);
};

export const config = {
  path: '/students',
};
