import React, { useEffect, useState, useContext } from "react";
import { getCourses, filterCourses } from "../api/courseApi";
import { getCart, addToCart, removeFromCart } from "../api/cartApi";
import { createCheckout } from "../api/paymentApi";
import { AuthContext } from "../context/AuthContext";
import { jwtDecode } from "jwt-decode";
import { Modal, Button, Alert, Card, Row, Col, Form } from "react-bootstrap";

const StudentPanel = () => {
  const { token } = useContext(AuthContext);
  const [courses, setCourses] = useState([]);
  const [cart, setCart] = useState([]);
  const [paid, setPaid] = useState([]);
  const [showModal, setShowModal] = useState(false);
  const [error, setError] = useState("");
  const [filters, setFilters] = useState({ name: "", category: "", minPrice: "", maxPrice: "" });
  const studentId = jwtDecode(token).sub;

  useEffect(() => {
    fetchCourses();
    fetchCart();
    fetchPaid();
    checkPaymentStatusFromUrl();
  }, []);

  const checkPaymentStatusFromUrl = () => {
    const params = new URLSearchParams(window.location.search);
    if (params.get("success")) {
      setShowModal(true);
      fetchCart();
      fetchPaid();
    }
    if (params.get("cancel")) {
      setError("Płatność została anulowana lub nie powiodła się.");
    }
  };

  const fetchCourses = async () => {
    const data = await getCourses();
    setCourses(data);
  };

  const fetchCart = async () => {
    const data = await getCart(studentId);
    setCart(data);
  };

  const fetchPaid = async () => {
    const res = await fetch(`http://localhost:8060/payment/student/${studentId}`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    const data = await res.json();
    setPaid(data);
  };

  const handleAdd = async (courseId, courseName) => {
    await addToCart({ studentId, courseId, courseName });
    fetchCart();
  };

  const handleRemove = async (id) => {
    await removeFromCart(id);
    fetchCart();
  };

  const handlePay = async (courseId, courseName) => {
    try {
      const { url } = await createCheckout(studentId, courseId, courseName);
      window.location.href = url;
    } catch (e) {
      setError("Wystąpił błąd podczas inicjowania płatności.");
    }
  };

  const handleFilter = async () => {
    const data = await filterCourses(filters);
    setCourses(data);
  };

  return (
    <div>
      <h4 className="mb-3">Dostępne kursy</h4>
      {error && <Alert variant="danger">{error}</Alert>}

      <Form className="mb-4">
        <Row>
          <Col md={4}>
            <Form.Control
              placeholder="Nazwa kursu"
              value={filters.name}
              onChange={(e) => setFilters({ ...filters, name: e.target.value })}
            />
          </Col>
          <Col md={3}>
            <Form.Control
              type="number"
              placeholder="Min cena"
              value={filters.minPrice}
              onChange={(e) => setFilters({ ...filters, minPrice: e.target.value })}
            />
          </Col>
          <Col md={3}>
            <Form.Control
              type="number"
              placeholder="Max cena"
              value={filters.maxPrice}
              onChange={(e) => setFilters({ ...filters, maxPrice: e.target.value })}
            />
          </Col>
          <Col md={2}>
            <Button variant="primary" onClick={handleFilter} className="w-100">Filtruj</Button>
          </Col>
        </Row>
      </Form>

      <Row>
        {courses.length > 0 ? courses.map((course) => (
          <Col md={4} className="mb-3" key={course.id}>
            <Card className="h-100 shadow-sm">
              <Card.Body>
                <Card.Title>{course.name}</Card.Title>
                <Card.Text>{course.description}</Card.Text>
                <Card.Text className="fw-bold">
                  Cena: {typeof course.price === "number" ? `${course.price.toFixed(2)} PLN` : "Brak ceny"}
                </Card.Text>
                <Button variant="outline-primary" className="w-100 btn-sm" onClick={() => handleAdd(course.id, course.name)}>
                  Dodaj do koszyka
                </Button>
              </Card.Body>
            </Card>
          </Col>
        )) : <p className="text-muted">Brak kursów do wyświetlenia</p>}
      </Row>

      <h4 className="mt-5 mb-3">Twój koszyk</h4>
      {cart.length > 0 ? (
        <>
          <ul className="list-group mb-2">
            {cart.map((item) => (
              <li key={item.id} className="list-group-item d-flex justify-content-between align-items-center">
                <span>{item.courseName}</span>
                <div>
                  <Button size="sm" variant="success" className="me-2" onClick={() => handlePay(item.courseId, item.courseName)}>Opłać</Button>
                  <Button size="sm" variant="danger" onClick={() => handleRemove(item.id)}>Usuń</Button>
                </div>
              </li>
            ))}
          </ul>
          <div className="text-end fw-bold">
            Suma: {cart.reduce((sum, item) => sum + parseFloat(item.coursePrice || 0), 0).toFixed(2)} PLN
          </div>
        </>
      ) : <p className="text-muted">Koszyk jest pusty</p>}

      <h4 className="mt-5 mb-3">Opłacone kursy</h4>
      {paid.length > 0 ? (
        <div className="table-responsive">
          <table className="table table-bordered table-striped shadow-sm">
            <thead className="table-light">
              <tr>
                <th>Nazwa kursu</th>
                <th className="text-center">ID kursu</th>
                <th className="text-center">Status</th>
              </tr>
            </thead>
            <tbody>
              {paid.map((item) => (
                <tr key={item.id}>
                  <td>{item.courseName}</td>
                  <td className="text-center">{item.courseId}</td>
                  <td className="text-center">
                    <span className={`badge 
                ${item.status === "COMPLETED" ? "bg-success" :
                        item.status === "FAILED" ? "bg-danger" :
                          item.status === "PENDING" ? "bg-warning text-dark" :
                            "bg-secondary"}`}>
                      {item.status}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
            <tfoot>
              <tr>
                <td colSpan="2" className="text-end fw-bold">Suma</td>
                <td className="text-center fw-bold">
                  {paid.reduce((sum, item) => sum + parseFloat(item.coursePrice || 0), 0).toFixed(2)} PLN
                </td>
              </tr>
            </tfoot>
          </table>
        </div>
      ) : <p className="text-muted">Brak opłaconych kursów</p>}

      <Modal show={showModal} onHide={() => setShowModal(false)}>
        <Modal.Header closeButton>
          <Modal.Title>Płatność zakończona</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          Kurs został pomyślnie opłacony i przeniesiony do sekcji "Opłacone kursy".
        </Modal.Body>
        <Modal.Footer>
          <Button variant="primary" onClick={() => setShowModal(false)}>Zamknij</Button>
        </Modal.Footer>
      </Modal>
    </div>
  );
};

export default StudentPanel;
