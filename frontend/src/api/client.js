async function request(path, options = {}) {
  const token = localStorage.getItem('token')
  const headers = { 'Content-Type': 'application/json', ...options.headers }
  if (token) headers.Authorization = `Bearer ${token}`

  const res = await fetch(path, { ...options, headers })

  if (res.status === 204) return null
  if (!res.ok) {
    const body = await res.json().catch(() => ({}))
    const err = new Error(body.message || `HTTP ${res.status}`)
    err.status = res.status
    throw err
  }
  return res.json()
}

export default request
