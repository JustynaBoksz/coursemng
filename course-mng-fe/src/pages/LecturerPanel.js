import React, { useEffect, useState, useContext } from "react";
import axios from "axios";
import { AuthContext } from "../context/AuthContext";
import { jwtDecode } from "jwt-decode";

const BASE_URL = "http://localhost:8060";

const LecturerPanel = () => {
  const { token } = useContext(AuthContext);
  const lecturerId = jwtDecode(token).sub;
  const [courses, setCourses] = useState([]);
  const [form, setForm] = useState({ name: "", description: "", price: "" });
  const [editingId, setEditingId] = useState(null);
  const [originalCourses, setOriginalCourses] = useState([]);

const fetchCourses = async () => {
  const res = await axios.get(`${BASE_URL}/courses`, {
    headers: { Authorization: `Bearer ${token}` },
  });
  setCourses(res.data);
  setOriginalCourses(res.data);
};

  useEffect(() => {
    fetchCourses();
  }, []);

  const handleCreateOrUpdate = async (e) => {
    e.preventDefault();
    const payload = {
      ...form,
      price: parseFloat(form.price).toFixed(2), 
      createdBy: lecturerId,
    };

    if (editingId) {
      await axios.put(`${BASE_URL}/courses/${editingId}`, payload, {
        headers: { Authorization: `Bearer ${token}` },
      });
    } else {
      await axios.post(`${BASE_URL}/courses`, payload, {
        headers: { Authorization: `Bearer ${token}` },
      });
    }
    setForm({ name: "", description: "", price: "" });
    setEditingId(null);
    fetchCourses();
  };

  const handleDelete = async (id) => {
    await axios.delete(`${BASE_URL}/courses/${id}`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    fetchCourses();
  };

  const handleEdit = (course) => {
    setForm({ name: course.name, description: course.description, price: course.price });
    setEditingId(course.id);
  };

  return (
    <div>
      <h4>{editingId ? "Edytuj kurs" : "Dodaj kurs"}</h4>
      <form onSubmit={handleCreateOrUpdate} className="card p-3 mb-4 shadow-sm">
        <div className="mb-2">
          <input
            placeholder="Nazwa"
            className="form-control"
            value={form.name}
            onChange={(e) => setForm({ ...form, name: e.target.value })}
            required
          />
        </div>
        <div className="mb-2">
          <input
            placeholder="Opis"
            className="form-control"
            value={form.description}
            onChange={(e) => setForm({ ...form, description: e.target.value })}
            required
          />
        </div>
        <div className="mb-2">
          <input
            type="number"
            step="0.01"
            placeholder="Cena w PLN (np. 50.00)"
            className="form-control"
            value={form.price}
            onChange={(e) => setForm({ ...form, price: e.target.value })}
            required
          />
        </div>
        <button className="btn btn-primary">{editingId ? "Zapisz zmiany" : "Dodaj"}</button>
      </form>

<h4 className="mt-4">Moje kursy</h4>

<div className="mb-3 row">
  <div className="col-md-4">
    <input
      className="form-control"
      placeholder="Filtruj po nazwie..."
      onChange={(e) => {
        const value = e.target.value.toLowerCase();
        const filtered = courses.filter((c) =>
          c.name.toLowerCase().includes(value)
        );
        setCourses(filtered.length > 0 || value ? filtered : originalCourses);
      }}
    />
  </div>
</div>

<div className="table-responsive">
  <table className="table table-bordered table-hover shadow-sm">
    <thead className="table-light">
      <tr>
        <th>Nazwa</th>
        <th>Opis</th>
        <th className="text-end">Cena [PLN]</th>
        <th>Akcje</th>
      </tr>
    </thead>
    <tbody>
      {courses.length > 0 ? (
        courses.map((course) => (
          <tr key={course.id}>
            <td>{course.name}</td>
            <td>{course.description}</td>
            <td className="text-end">{parseFloat(course.price).toFixed(2)}</td>
            <td>
              <button
                onClick={() => handleEdit(course)}
                className="btn btn-sm btn-warning me-2"
              >
                Edytuj
              </button>
              <button
                onClick={() => handleDelete(course.id)}
                className="btn btn-sm btn-danger"
              >
                Usuń
              </button>
            </td>
          </tr>
        ))
      ) : (
        <tr>
          <td colSpan="4" className="text-center text-muted">
            Brak kursów do wyświetlenia
          </td>
        </tr>
      )}
    </tbody>
  </table>
</div>

    </div>
  );
};

export default LecturerPanel;
