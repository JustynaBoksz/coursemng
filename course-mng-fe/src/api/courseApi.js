import axios from "axios";

const BASE_URL = "http://localhost:8060";

export const getCourses = () => axios.get(`${BASE_URL}/courses`).then(res => res.data);
export const searchCourses = (name) => axios.get(`${BASE_URL}/courses/search?name=${name}`).then(res => res.data);
export const filterCourses = (filters) => {
  const params = new URLSearchParams();

  if (filters.name) params.append("name", filters.name);
  if (filters.minPrice) params.append("minPrice", filters.minPrice);
  if (filters.maxPrice) params.append("maxPrice", filters.maxPrice);

  return axios
    .get(`${BASE_URL}/courses/filter?${params.toString()}`)
    .then((res) => res.data);
};
export const updateCourse = (id, updated) => axios.put(`${BASE_URL}/courses/${id}`, updated);
