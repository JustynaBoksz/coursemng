import axios from "axios";

const BASE_URL = "http://localhost:8060";

export const startPayment = (studentId, courseId) =>
    axios.post(`${BASE_URL}/payment/start`, { studentId, courseId }).then(res => res.data);
export const createCheckout = (studentId, courseId, courseName) =>
  axios.post(`${BASE_URL}/payment/checkout`, { studentId, courseId, courseName }).then(res => res.data);

