import React from "react";
import { useNavigate } from "react-router-dom";

const ErrorPage = ({ code = "Error", message = "Wystąpił nieoczekiwany błąd" }) => {
  const navigate = useNavigate();

  return (
    <div className="container text-center mt-5">
      <h1 className="display-1">{code}</h1>
      <p className="lead">{message}</p>
      <button className="btn btn-primary mt-3" onClick={() => navigate("/dashboard")}>Wróć do panelu</button>
    </div>
  );
};

export default ErrorPage;