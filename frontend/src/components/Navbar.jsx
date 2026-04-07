import { useContext } from "react";
import { Link } from "react-router-dom";
import { AuthContext } from "../context/auth-context";
import "./Navbar.css";

function Navbar() {
  const { user, setUser } = useContext(AuthContext);
  const dashboardPath =
    user?.role === "doctor"
      ? "/doctor/dashboard"
      : user?.role === "admin"
      ? "/admin/dashboard"
      : "/patient/dashboard";

  const quickPath =
    user?.role === "doctor"
      ? "/doctor/manage-queue"
      : user?.role === "admin"
      ? "/admin/database"
      : "/patient/book";

  return (
    <nav className="topbar">
      <div className="topbar__inner">
        <Link className="topbar__brand" to="/">
          <span className="topbar__brand-mark">SQ</span>
          <span className="topbar__brand-copy">
            <span className="topbar__title">Smart Queue</span>
            <span className="topbar__subtitle">Queue-free hospital appointments</span>
          </span>
        </Link>

        <div className="topbar__links">
          <Link className="topbar__link" to="/">
            Home
          </Link>
          {user ? (
            <>
              <Link className="topbar__link" to={dashboardPath}>
                Dashboard
              </Link>
              <Link className="topbar__link" to={quickPath}>
                {user.role === "doctor"
                  ? "Queue"
                  : user.role === "admin"
                  ? "Database"
                  : "Book"}
              </Link>
              <button className="topbar__link topbar__button" onClick={() => setUser(null)}>
                Logout
              </button>
            </>
          ) : (
            <>
              <Link className="topbar__link" to="/login">
                Login
              </Link>
              <Link className="topbar__link topbar__link--primary" to="/register">
                Register
              </Link>
            </>
          )}
        </div>
      </div>
    </nav>
  );
}

export default Navbar;
