import axios from "axios";

const BASE_URL = "http://localhost:8060";

export const addToCart = (item) => axios.post(`${BASE_URL}/cart/add`, item);
export const getCart = (studentId) => axios.get(`${BASE_URL}/cart/student/${studentId}`).then(res => res.data);
export const removeFromCart = (id) => axios.delete(`${BASE_URL}/cart/${id}`);
