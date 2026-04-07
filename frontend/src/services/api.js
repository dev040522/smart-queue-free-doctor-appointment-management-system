import axios from "axios";

export const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080/api";

export const getApiOrigin = () => {
  try {
    return new URL(API_BASE_URL).origin;
  } catch {
    return API_BASE_URL;
  }
};

const API = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
});

export const registerUser = async (payload) => {
  const response = await API.post("/users/register", payload);
  return response.data;
};

export const loginUser = async (payload) => {
  const response = await API.post("/users/login", payload);
  return response.data;
};

export const getDoctors = async () => {
  const response = await API.get("/doctors");
  return response.data;
};

export const getUsers = async () => {
  const response = await API.get("/users");
  return response.data;
};

export const deleteUser = async (userId) => {
  const response = await API.delete(`/users/${encodeURIComponent(userId)}`);
  return response.data;
};

export const updateAdminUser = async (userId, payload) => {
  const response = await API.put(`/admin/users/${encodeURIComponent(userId)}`, payload);
  return response.data;
};

export const deleteAdminUser = async (userId) => {
  const response = await API.delete(`/admin/users/${encodeURIComponent(userId)}`);
  return response.data;
};

export const getAppointments = async (params = {}) => {
  const response = await API.get("/appointments", { params });
  return response.data;
};

export const createAppointment = async (payload) => {
  const response = await API.post("/appointments", payload);
  return response.data;
};

export const getAllAppointments = async () => {
  return getAppointments();
};

export const updateAppointmentStatus = async (appointmentId, status) => {
  const response = await API.patch(`/appointments/${appointmentId}/status`, { status });
  return response.data;
};

export const updateAdminDoctor = async (doctorId, payload) => {
  const response = await API.put(`/admin/doctors/${doctorId}`, payload);
  return response.data;
};

export const deleteAdminDoctor = async (doctorId) => {
  const response = await API.delete(`/admin/doctors/${doctorId}`);
  return response.data;
};

export const deleteAdminAppointment = async (appointmentId) => {
  const response = await API.delete(`/admin/appointments/${appointmentId}`);
  return response.data;
};

export const getQueue = async () => {
  const response = await API.get("/appointments/queue");
  return response.data;
};

export default API;
