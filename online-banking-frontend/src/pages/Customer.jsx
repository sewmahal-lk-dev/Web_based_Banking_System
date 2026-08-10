import { useState, useEffect } from 'react';
import { Plus, Search, Mail, UserCheck, X } from 'lucide-react';
import API from '../api';

export default function Customer() {
    const [customers, setCustomers] = useState([]);
    const [showModal, setShowModal] = useState(false);

    // Form Fields
    const [username, setUsername] = useState('');
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');

    // Backend එකෙන් Users ලා ගෙන්න ගැනීම
    useEffect(() => {
        let isMounted = true;

        API.get('/users')
            .then((response) => {
                if (isMounted) {
                    setCustomers(response.data);
                }
            })
            .catch((error) => {
                console.error("Error fetching users:", error);
            });

        return () => {
            isMounted = false;
        };
    }, []);

    const refreshCustomers = async () => {
        try {
            const response = await API.get('/users');
            setCustomers(response.data);
        } catch (error) {
            console.error("Error fetching users:", error);
        }
    };

    // අලුත් User කෙනෙක් Save කිරීම
    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            await API.post('/users/register', {
                username: username,
                email: email,
                password: password
            });
            refreshCustomers();
            setShowModal(false);
            setUsername('');
            setEmail('');
            setPassword('');
            alert('User Registered Successfully!');
        } catch (error) {
            console.error("Error registering user:", error);
            alert('Failed to register user.');
        }
    };

    return (
        <div className="p-6">
            <div className="flex justify-between items-center mb-6">
                <div>
                    <h1 className="text-2xl font-bold text-gray-800">Bank Customers</h1>
                    <p className="text-gray-500 text-sm">Manage registered bank customers</p>
                </div>
                <button
                    onClick={() => setShowModal(true)}
                    className="flex items-center gap-2 bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg font-medium shadow-md transition cursor-pointer">
                    <Plus size={18} /> Add New Customer
                </button>
            </div>

            <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
                <div className="p-4 border-b border-gray-100 flex items-center gap-3">
                    <Search className="text-gray-400" size={20} />
                    <input
                        type="text"
                        placeholder="Search customer..."
                        className="w-full bg-transparent outline-none text-gray-700"
                    />
                </div>

                <table className="w-full text-left border-collapse">
                    <thead>
                    <tr className="bg-gray-50 text-gray-600 text-sm">
                        <th className="p-4">User ID</th>
                        <th className="p-4">Username</th>
                        <th className="p-4">Email</th>
                    </tr>
                    </thead>
                    <tbody className="divide-y divide-gray-100">
                    {customers.length > 0 ? (
                        customers.map((c) => (
                            <tr key={c.id} className="hover:bg-gray-50/50">
                                <td className="p-4 font-semibold text-gray-700">#{c.id}</td>
                                <td className="p-4 font-medium flex items-center gap-2 text-gray-800">
                                    <UserCheck className="text-blue-500" size={18} />
                                    {c.username}
                                </td>
                                <td className="p-4 text-gray-600">
                                    <span className="flex items-center gap-2"><Mail size={14} className="text-gray-400"/>{c.email}</span>
                                </td>
                            </tr>
                        ))
                    ) : (
                        <tr>
                            <td colSpan="3" className="p-4 text-center text-gray-500">No customers found in Database.</td>
                        </tr>
                    )}
                    </tbody>
                </table>
            </div>

            {showModal && (
                <div className="fixed inset-0 bg-black/50 flex justify-center items-center z-50">
                    <div className="bg-white p-6 rounded-xl w-96 shadow-xl relative">
                        <button
                            onClick={() => setShowModal(false)}
                            className="absolute top-4 right-4 text-gray-400 hover:text-gray-600 cursor-pointer">
                            <X size={20} />
                        </button>
                        <h2 className="text-xl font-bold mb-4 text-gray-800">Register New Customer</h2>

                        <form onSubmit={handleSubmit} className="space-y-4">
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">Username</label>
                                <input
                                    type="text"
                                    required
                                    placeholder="e.g. saman_p"
                                    value={username}
                                    onChange={(e) => setUsername(e.target.value)}
                                    className="w-full border border-gray-300 p-2 rounded-lg outline-none focus:border-blue-500"
                                />
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">Email</label>
                                <input
                                    type="email"
                                    required
                                    placeholder="saman@gmail.com"
                                    value={email}
                                    onChange={(e) => setEmail(e.target.value)}
                                    className="w-full border border-gray-300 p-2 rounded-lg outline-none focus:border-blue-500"
                                />
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">Password</label>
                                <input
                                    type="password"
                                    required
                                    placeholder="******"
                                    value={password}
                                    onChange={(e) => setPassword(e.target.value)}
                                    className="w-full border border-gray-300 p-2 rounded-lg outline-none focus:border-blue-500"
                                />
                            </div>

                            <button
                                type="submit"
                                className="w-full bg-blue-600 hover:bg-blue-700 text-white py-2 rounded-lg font-medium transition cursor-pointer">
                                Register Customer
                            </button>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
}