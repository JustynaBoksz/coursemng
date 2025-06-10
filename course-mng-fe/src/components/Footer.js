import React, { useEffect, useState } from "react";
import axios from "axios";
import { Card, Spinner, Alert } from "react-bootstrap";


export const FooterInfo = () => {
  const [holiday, setHoliday] = useState(null);
  const [error, setError] = useState("");
  const today = new Date().toLocaleDateString("pl-PL", {
    year: "numeric",
    month: "long",
    day: "numeric"
  });

  useEffect(() => {
    const fetchHoliday = async () => {
      try {
        const res = await axios.get("http://localhost:8060/holiday");
        setHoliday(res.data);
      } catch {
        setError("Błąd pobierania informacji o święcie.");
      }
    };
    fetchHoliday();
  }, []);

  return (
    <footer className="text-center mt-5 py-3 border-top">
      <small>
        📅 Dzisiaj: {today}
        {holiday?.isHoliday ? ` – Święto: ${holiday.name}` : " – Brak święta"}
      </small>
      {error && <div className="text-danger small">{error}</div>}
    </footer>
  );
};
