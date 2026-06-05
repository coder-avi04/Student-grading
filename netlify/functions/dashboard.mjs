import { getDatabase } from '@netlify/database';

export default async (req, context) => {
  if (context.cookies.get('loggedInUser') !== 'admin') {
    return Response.json({ status: 'error', message: 'Unauthorized. Please login.' }, { status: 401 });
  }

  const db = getDatabase();
  const rows = await db.sql`SELECT average, grade FROM students`;

  const total = rows.length;
  const counts = { A: 0, B: 0, C: 0, D: 0, F: 0 };
  const gradePriority = { A: 5, B: 4, C: 3, D: 2, F: 1 };
  let topGrade = total ? 'F' : 'N/A';
  let totalAvg = 0;

  for (const row of rows) {
    const g = row.grade;
    counts[g] = (counts[g] || 0) + 1;
    totalAvg += Number(row.average);
    if (total && gradePriority[g] > gradePriority[topGrade]) {
      topGrade = g;
    }
  }

  const classAverage = total ? Math.round((totalAvg / total) * 100) / 100 : 0;

  return Response.json({
    totalStudents: total,
    classAverage,
    topGrade,
    gradeA: counts.A || 0,
    gradeB: counts.B || 0,
    gradeC: counts.C || 0,
    gradeD: counts.D || 0,
    gradeF: counts.F || 0,
  });
};

export const config = {
  path: '/dashboard',
};
