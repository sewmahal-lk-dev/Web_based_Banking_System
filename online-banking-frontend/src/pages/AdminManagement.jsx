import { useState } from 'react';
import { ShieldCheck, Users, Shield } from 'lucide-react';

export default function AdminManagement() {
    const [users, setUsers] = useState([
        { id: 'USR-101', name: 'Pathum M.G.D.D.', email: 'pathum@apex.lk', role: 'LOAN_OFFICER' },
        { id: 'USR-102', name: 'Mathuziyanth T.', email: 'mathu@apex.lk', role: 'CARD_OFFICER' },
        { id: 'USR-103', name: 'Gunarathna W.', email: 'gunarathna@apex.lk', role: 'INVESTMENT_OFFICER' },
        { id: 'USR-104', name: 'Dassanayaka D.M.H.S.', email: 'admin@apex.lk', role: 'ADMIN' },
        { id: 'USR-105', name: 'Kasun Perera', email: 'kasun@gmail.com', role: 'CUSTOMER' }
    ]);

    const handleRoleChange = (id, newRole) => {
        setUsers(users.map(u => u.id === id ? { ...u, role: newRole } : u));
        alert(`User role updated to ${newRole}!`);
    };

    return (
        <div className="space-y-8">
            <div>
                <h1 className="text-3xl font-extrabold text-white flex items-center gap-3">
                    <ShieldCheck className="text-cyan-400" size={32} />
                    System Administration & Role Management
                </h1>
                <p className="text-slate-400 text-sm mt-1">Admin Control Center</p>
            </div>

            <div className="bg-slate-950 p-6 rounded-2xl border border-slate-800 shadow-xl space-y-4">
                <h2 className="text-xl font-bold text-white flex items-center gap-2">
                    <Users size={22} className="text-cyan-400" /> User Roles & Access Control
                </h2>

                <div className="overflow-x-auto">
                    <table className="w-full text-left text-slate-300">
                        <thead className="bg-slate-900 text-slate-400 text-xs uppercase border-b border-slate-800">
                        <tr>
                            <th className="p-3">User</th>
                            <th className="p-3">Email</th>
                            <th className="p-3">Current Role</th>
                            <th className="p-3 text-right">Assign New Role</th>
                        </tr>
                        </thead>
                        <tbody className="divide-y divide-slate-800 text-sm">
                        {users.map((u) => (
                            <tr key={u.id} className="hover:bg-slate-900/50">
                                <td className="p-3">
                                    <p className="font-bold text-white">{u.name}</p>
                                    <span className="text-xs text-slate-500 font-mono">{u.id}</span>
                                </td>
                                <td className="p-3 text-slate-300">{u.email}</td>
                                <td className="p-3">
                    <span className="px-3 py-1 bg-cyan-500/10 text-cyan-400 rounded-full text-xs font-bold border border-cyan-500/20">
                      {u.role}
                    </span>
                                </td>
                                <td className="p-3 text-right">
                                    <select
                                        value={u.role}
                                        onChange={(e) => handleRoleChange(u.id, e.target.value)}
                                        className="bg-slate-900 border border-slate-700 text-white text-xs rounded-xl p-2 outline-none cursor-pointer hover:border-cyan-400 transition">
                                        <option value="CUSTOMER">CUSTOMER</option>
                                        <option value="LOAN_OFFICER">LOAN_OFFICER</option>
                                        <option value="CARD_OFFICER">CARD_OFFICER</option>
                                        <option value="INVESTMENT_OFFICER">INVESTMENT_OFFICER</option>
                                        <option value="SUPPORT_OFFICER">SUPPORT_OFFICER</option>
                                        <option value="ACCOUNT_OFFICER">ACCOUNT_OFFICER</option>
                                        <option value="ADMIN">ADMIN</option>
                                    </select>
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    );
}