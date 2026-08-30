import { useEffect, useState } from "react";
import { fetchUsers, type User } from "../api/users";

export function UserList() {
  const [users, setUsers] = useState<User[] | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchUsers()
      .then(setUsers)
      .catch((err: unknown) => {
        setError(err instanceof Error ? err.message : "不明なエラーが発生しました");
      });
  }, []);

  if (error) {
    return <p>{error}</p>;
  }

  if (users === null) {
    return <p>Loading...</p>;
  }

  if (users.length === 0) {
    return <p>ユーザーが見つかりませんでした。</p>;
  }

  return (
    <table>
      <thead>
        <tr>
          <th>id</th>
          <th>employeeId</th>
          <th>name</th>
          <th>admin</th>
        </tr>
      </thead>
      <tbody>
        {users.map((user) => (
          <tr key={user.id}>
            <td>{user.id}</td>
            <td>{user.employeeId}</td>
            <td>{user.name}</td>
            <td>{user.admin ? "◯" : "-"}</td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}
