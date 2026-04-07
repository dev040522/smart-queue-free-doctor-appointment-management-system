import { Link } from "react-router-dom";

function Dashboard() {
  const quickActions = [
    {
      id: "book",
      title: "Book Appointment",
      description: "Schedule your appointment with available doctors.",
      to: "/patient/book",
      badge: "AP",
    },
    {
      id: "queue",
      title: "Queue Status",
      description: "Check your position in the queue in real-time.",
      to: "/patient/queue",
      badge: "QS",
    },
    {
      id: "history",
      title: "Appointment History",
      description: "View your past appointments and records.",
      to: "/patient/history",
      badge: "AH",
    },
  ];

  return (
    <div className="p-6">
      <h1 className="text-3xl font-bold mb-6 text-gray-800">Patient Dashboard</h1>

      <div className="grid sm:grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {quickActions.map((action) => (
          <Link
            key={action.id}
            to={action.to}
            className="bg-white/70 backdrop-blur-md shadow-lg rounded-xl p-6 flex flex-col items-center hover:scale-105 transition-transform"
          >
            <div className="w-16 h-16 mb-4 rounded-full bg-blue-100 text-blue-700 flex items-center justify-center font-bold">
              {action.badge}
            </div>
            <h2 className="text-xl font-semibold mb-2">{action.title}</h2>
            <p className="text-gray-600 text-center">{action.description}</p>
          </Link>
        ))}
      </div>
    </div>
  );
}

export default Dashboard;
