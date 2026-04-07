import { Link } from "react-router-dom";

function NotFound() {
  return (
    <section>
      <h1>Page not found</h1>
      <p>The page you requested does not exist yet.</p>
      <p>
        <Link to="/">Return home</Link>
      </p>
    </section>
  );
}

export default NotFound;
