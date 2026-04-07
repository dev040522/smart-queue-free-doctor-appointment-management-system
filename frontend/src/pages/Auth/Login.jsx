import { useContext, useState } from "react";
import { useNavigate } from "react-router-dom";
import { AuthContext } from "../../context/auth-context";
import { ToastContext } from "../../context/toast-context";
import { getApiOrigin, loginUser } from "../../services/api";

function Login() {
  const navigate = useNavigate();
  const { setUser } = useContext(AuthContext);
  const { showToast } = useContext(ToastContext);
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(false);

  const handleLogin = async (e) => {
    e.preventDefault();
    setLoading(true);
    setMessage("");

    try {
      const response = await loginUser({ email, password });
      setUser(response.user);
      setMessage(response.message);
      showToast(`Login successful. Welcome ${response.user?.name || "back"}.`);
      navigate(
        response.user?.role === "doctor" ? "/doctor/dashboard" : "/patient/dashboard"
      );
    } catch (error) {
      if (!error.response) {
        setMessage(`Backend server is not running on ${getApiOrigin()}. Start it and try again.`);
      } else {
        setMessage(error.response?.data?.message || "Login failed.");
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-md mx-auto mt-10 p-6 shadow-lg rounded-lg">
      <h2 className="text-xl font-bold mb-4">Login</h2>
      <form onSubmit={handleLogin} className="space-y-4">
        <input
          type="email"
          placeholder="Email"
          className="w-full p-2 border rounded"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
        />
        <input
          type="password"
          placeholder="Password"
          className="w-full p-2 border rounded"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
        />
        <button className="w-full bg-blue-600 text-white py-2 rounded" disabled={loading}>
          {loading ? "Signing in..." : "Login"}
        </button>
      </form>

      {message && <p className="mt-4 text-center text-gray-700">{message}</p>}
    </div>
  );
}

export default Login;
