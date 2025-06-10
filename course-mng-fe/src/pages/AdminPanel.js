import React, { useEffect, useState, useContext } from "react";
import { AuthContext } from "../context/AuthContext";
import axios from "axios";
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, Legend } from "recharts";
import { Card, ListGroup, Container, Alert } from "react-bootstrap";

const BASE_URL = "http://localhost:8060";

const AdminPanel = () => {
  const { token } = useContext(AuthContext);
  const [courses, setCourses] = useState([]);
  const [paymentsGrouped, setPaymentsGrouped] = useState({});

  const fetchData = async () => {
    const resCourses = await axios.get(`${BASE_URL}/courses/popular`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    setCourses(resCourses.data);

    const resPayments = await axios.get(`${BASE_URL}/payment/grouped`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    setPaymentsGrouped(resPayments.data);
  };

  useEffect(() => {
    fetchData();
  }, []);

  return (
    <Container>
      <Card className="p-4 mb-4 shadow-sm">
        <h4 className="mb-3">Popularność kursów</h4>
        {courses.length === 0 ? (
          <Alert variant="info">Brak danych o popularności kursów.</Alert>
        ) : (
          <>
            <ListGroup className="mb-4">
              {courses.map((course) => (
                <ListGroup.Item key={course.id} className="d-flex justify-content-between">
                  <span>{course.name}</span>
                  <span>{course.enrolledCount} zapisanych</span>
                </ListGroup.Item>
              ))}
            </ListGroup>

            <div className="mb-4">
              <ResponsiveContainer width="100%" height={300}>
                <BarChart data={courses}>
                  <XAxis dataKey="name" />
                  <YAxis allowDecimals={false} />
                  <Tooltip />
                  <Legend />
                  <Bar dataKey="enrolledCount" fill="#8884d8" />
                </BarChart>
              </ResponsiveContainer>
            </div>
          </>
        )}
      </Card>

      <Card className="p-4 shadow-sm">
        <h4 className="mb-3">Statusy płatności</h4>
        {Object.keys(paymentsGrouped).length === 0 ? (
          <Alert variant="info">Brak informacji o płatnościach.</Alert>
        ) : (
          Object.entries(paymentsGrouped).map(([status, payments]) => (
            <div key={status} className="mb-3">
              <h5 className="text-capitalize">{status.toLowerCase()}</h5>
              <ListGroup>
                {payments.map((p) => (
                  <ListGroup.Item key={p.id}>
                    Student: {p.studentId}, Kurs: {p.courseId}, Data: {new Date(p.timestamp).toLocaleString()}
                  </ListGroup.Item>
                ))}
              </ListGroup>
            </div>
          ))
        )}
      </Card>
    </Container>
  );
};

export default AdminPanel;
