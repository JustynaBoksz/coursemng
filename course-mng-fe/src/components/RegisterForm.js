import React, { useState, useContext } from "react";
import { login, register } from "../api/authApi";
import { AuthContext } from "../context/AuthContext";
import { useNavigate } from "react-router-dom";

const RegisterForm = () => {
  const { setToken, setRole } = useContext(AuthContext);
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [role, setRoleParam] = useState("");
  const [error, setError] = useState("");
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await register({ username, email, password, role });
      const loginRes = await login({ username, password });
      const token = loginRes.data.token;
      const payload = JSON.parse(atob(token.split(".")[1]));
      setToken(token);
      setRole(payload.role);
      navigate("/dashboard");
    } catch (err) {
      setError("B\u0142\u0119dne dane rejestracji");
    }
  };

  return (
    <div className="d-flex align-items-center justify-content-center vh-100 bg-light">
      <div className="card p-4 shadow-lg" style={{ minWidth: "350px", maxWidth: "450px" }}>
        <h3 className="text-center mb-4">Rejestracja</h3>
        {error && <div className="alert alert-danger">{error}</div>}
        <form onSubmit={handleSubmit}>
          <div className="mb-3">
            <label className="form-label">Nazwa użytkownika</label>
            <input className="form-control" value={username} onChange={(e) => setUsername(e.target.value)} required />
          </div>
          <div className="mb-3">
            <label className="form-label">E-mail</label>
            <input type="email" className="form-control" value={email} onChange={(e) => setEmail(e.target.value)} required />
          </div>
          <div className="mb-3">
            <label className="form-label">Hasło</label>
            <input type="password" className="form-control" value={password} onChange={(e) => setPassword(e.target.value)} required />
          </div>
          <div className="mb-3">
            <label className="form-label">Rola</label>
            <select className="form-select" value={role} onChange={(e) => setRoleParam(e.target.value)} required>
              <option value="">Wybierz rolę</option>
              <option value="STUDENT">Student</option>
              <option value="LECTURER">Wykładowca</option>
            </select>
          </div>
          <button type="submit" className="btn btn-primary w-100">Zarejestruj się</button>
        </form>
      </div>
    </div>
  );
};

export default RegisterForm;