import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import Navbar from "./components/Navbar";

import Home from "./pages/Home";
import Login from "./pages/Auth/Login";
import Register from "./pages/Auth/Register";

import PatientDashboard from "./pages/Patient/Dashboard";
import BookAppointment from "./pages/Patient/BookAppointment";
import QueueStatus from "./pages/Patient/QueueStatus";
import AppointmentHistory from "./pages/Patient/History";

import DoctorDashboard from "./pages/Doctor/Dashboard";
import ManageQueue from "./pages/Doctor/ManageQueue";

import AdminDashboard from "./pages/Admin/Dashboard";
import ManageUsers from "./pages/Admin/ManageUsers";
import NotFound from "./pages/NotFound";

function App() {
  return (
    <Router>
      <Navbar />
      <div className="p-4">
        <Routes>
          <Route path="/" element={<Home />} />

          {/* Auth */}
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />

          {/* Patient */}
          <Route path="/patient/dashboard" element={<PatientDashboard />} />
          <Route path="/patient/book" element={<BookAppointment />} />
          <Route path="/patient/queue" element={<QueueStatus />} />
          <Route path="/patient/history" element={<AppointmentHistory />} />

          {/* Doctor */}
          <Route path="/doctor/dashboard" element={<DoctorDashboard />} />
          <Route path="/doctor/manage-queue" element={<ManageQueue />} />

          {/* Admin */}
          <Route path="/admin/dashboard" element={<AdminDashboard />} />
          <Route path="/admin/manage-users" element={<ManageUsers />} />
          <Route path="/admin/database" element={<ManageUsers />} />
          <Route path="*" element={<NotFound />} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;
