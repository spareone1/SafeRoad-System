// Minimal helpers to call APIs without touching existing templates.
async function apiLogin(userid, pw) {
  const res = await fetch('/api/auth/login', {
    method: 'POST',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify({userid, pw})
  });
  const data = await res.json();
  if (data.ok) {
    localStorage.setItem('access', data.access);
    localStorage.setItem('refresh', data.refresh);
  }
  return data;
}

async function apiChangePassword(currentPassword, newPassword) {
  const token = localStorage.getItem('access');
  const res = await fetch('/api/users/me/password', {
    method: 'POST',
    headers: {'Content-Type': 'application/json', 'Authorization': 'Bearer ' + token},
    body: JSON.stringify({currentPassword, newPassword})
  });
  return await res.json();
}
