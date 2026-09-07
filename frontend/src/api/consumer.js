import request from './index'

export function verifyCode(code) {
  return request.get('/v2/trace/verify', { params: { code } })
}

export function traceByBatch(batchNumber) {
  return request.get(`/v2/trace/batch/${encodeURIComponent(batchNumber)}`)
}

export function submitComplaint(data) {
  return request.post('/complaint', data)
}
