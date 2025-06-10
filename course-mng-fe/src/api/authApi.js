import axios from "axios";

const BASE_URL = "http://localhost:8060";

export const login = (data) => axios.post(`${BASE_URL}/auth/login`, data);
export const register = (data) => axios.post(`${BASE_URL}/auth/register`, data);
