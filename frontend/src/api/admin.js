import request from './index'

export function storeCaptcha(username, captcha) {
  return request.post('/captcha', { username, captcha })
}

export function login(username, password, captcha) {
  return request.post('/login', { username, password, captcha })
}

export function registerAdmin(data) {
  return request.post('/admin/register', data)
}

export function getAdmins() {
  return request.get('/admins')
}

export function deleteAdmin(id) {
  return request.delete(`/admins/${id}`)
}

export function getProducts(params) {
  return request.get('/products', { params })
}

export function getProduct(id) {
  return request.get(`/products/${id}`)
}

export function createProduct(data) {
  return request.post('/products', data)
}

export function updateProduct(id, data) {
  return request.put(`/products/${id}`, data)
}

export function deleteProduct(id) {
  return request.delete(`/products/${id}`)
}

export function hardDeleteProduct(id) {
  return request.delete(`/products/${id}/hard`)
}

export function getMaterialVarieties(params) {
  return request.get('/v2/material-varieties', { params })
}

export function getMaterialVariety(id) {
  return request.get(`/v2/material-varieties/${id}`)
}

export function createMaterialVariety(data) {
  return request.post('/v2/material-varieties', data)
}

export function updateMaterialVariety(id, data) {
  return request.put(`/v2/material-varieties/${id}`, data)
}

export function deleteMaterialVariety(id) {
  return request.delete(`/v2/material-varieties/${id}`)
}

export function activateMaterialVariety(id) {
  return request.post(`/v2/material-varieties/${id}/activate`)
}

export function deactivateMaterialVariety(id) {
  return request.post(`/v2/material-varieties/${id}/deactivate`)
}

export function getMaterialPurchases(params) {
  return request.get('/v2/material-purchases', { params })
}

export function getMaterialPurchase(id) {
  return request.get(`/v2/material-purchases/${id}`)
}

export function createMaterialPurchase(data) {
  return request.post('/v2/material-purchases', data)
}

export function updateMaterialPurchase(id, data) {
  return request.put(`/v2/material-purchases/${id}`, data)
}

export function deleteMaterialPurchase(id) {
  return request.delete(`/v2/material-purchases/${id}`)
}

export function getBatches(params) {
  return request.get('/v2/batches', { params })
}

export function getBatchSelectOptions(params) {
  return request.get('/v2/batches/select-options', { params })
}

export function getBatch(id) {
  return request.get(`/v2/batches/${id}`)
}

export function getBatchDetail(id) {
  return request.get(`/v2/batches/${id}/detail`)
}

export function getBatchByNumber(batchNumber) {
  return request.get(`/v2/batches/by-number/${encodeURIComponent(batchNumber)}`)
}

export function createBatch(data) {
  return request.post('/v2/batches', data)
}

export function deleteBatch(id) {
  return request.delete(`/v2/batches/${id}`)
}

export function generateSecurityCodes(batchId, data) {
  return request.post(`/batches/${batchId}/security-codes`, data)
}

export function getSecurityCodes(batchId) {
  return request.get(`/batches/${batchId}/security-codes`)
}

export function exportSecurityCodes(batchId) {
  return request.get(`/security-codes/export/${batchId}`)
}

export function getStorages() {
  return request.get('/v2/storage')
}

export function createStorage(data) {
  return request.post('/v2/storage', data)
}

export function getTransportSales() {
  return request.get('/v2/transport-sales')
}

export function createTransportSale(data) {
  return request.post('/v2/transport-sales', data)
}

export function getInspections() {
  return request.get('/v2/inspections')
}

export function createInspection(data) {
  return request.post('/v2/inspections', data)
}

export function getComplaints() {
  return request.get('/getAllComplaintInfo')
}

export function deleteComplaint(id) {
  return request.delete(`/deleteComplaintInfo/${id}`)
}

export function batchDeleteComplaints(ids) {
  return request.delete('/deleteComplaintInfo/batch', { data: ids })
}

export function getBlockchainPublicKey() {
  return request.get('/blockchain/public-key')
}

export function getBlockchainSummary() {
  return request.get('/blockchain/monitor/summary')
}

export function getAgents() {
  return request.get('/agent/list')
}

export function getAgent(id) {
  return request.get(`/agent/${id}`)
}

export function getConsensusStatus() {
  return request.get('/agent/consensus/status')
}

export function getReputationList() {
  return request.get('/agent/reputation/list')
}

export function getAgentReputation(agentId) {
  return request.get(`/agent/${agentId}/reputation`)
}

export function getCompanies() {
  return request.get('/companies')
}

export function getCompany(id) {
  return request.get(`/companies/${id}`)
}

export function createCompany(data) {
  return request.post('/companies', data)
}

export function updateCompany(id, data) {
  return request.put(`/companies/${id}`, data)
}

export function deleteCompany(id) {
  return request.delete(`/companies/${id}`)
}
