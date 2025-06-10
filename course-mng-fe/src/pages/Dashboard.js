import { useContext } from "react";
import { AuthContext } from "../context/AuthContext";
import StudentPanel from "./StudentPanel";
import LecturerPanel from "./LecturerPanel";
import AdminPanel from "./AdminPanel";
import { Container, Card, Button } from "react-bootstrap";
import { FooterInfo } from "../components/Footer";

const Dashboard = () => {
  const { role, logout } = useContext(AuthContext);

  const renderPanel = () => {
    switch (role) {
      case "STUDENT":
        return <StudentPanel />;
      case "LECTURER":
        return <LecturerPanel />;
      case "ADMIN":
        return <AdminPanel />;
      default:
        return <p>Brak przypisanej roli</p>;
    }
  };

  return (
    <>
    <Container className="py-5">
      <Card className="shadow-sm p-4 mb-4">
        <div className="d-flex justify-content-between align-items-center">
          <h3 className="mb-0">Witaj w panelu: <span className="text-capitalize">{role.toLowerCase()}</span></h3>
          <Button variant="outline-danger" onClick={logout}>Wyloguj</Button>
        </div>
      </Card>

      <Card className="p-4 shadow-sm">
        {renderPanel()}
      </Card>
    </Container>

    <FooterInfo />
    </>
  );
};

export default Dashboard;
