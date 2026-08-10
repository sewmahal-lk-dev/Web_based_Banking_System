import { useState, useEffect } from 'react';
import API from '../api';

const LoanManagement = ({ isOfficer = false }) => {
    const currentUserId = localStorage.getItem('userId') || 1;
    const [loans, setLoans] = useState([]);
    const [formData, setFormData] = useState({
        userId: currentUserId,
        loanType: 'Personal Loan',
        amount: '',
        durationMonths: ''
    });

    const fetchLoans = async () => {
        try {
            const endpoint = isOfficer ? '/loans/all' : `/loans/my-loans/${currentUserId}`;
            const response = await API.get(endpoint);
            setLoans(response.data);
        } catch (error) {
            console.error(error);
        }
    };

    useEffect(() => {
        void fetchLoans();
    }, [isOfficer]);

    const handleChange = (e) => setFormData(prev => ({ ...prev, [e.target.name]: e.target.value }));

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            await API.post('/loans/apply', { ...formData, userId: currentUserId });
            alert('Submitted!');
            setFormData({ userId: currentUserId, loanType: 'Personal Loan', amount: '', durationMonths: '' });
            await fetchLoans();
        } catch (err) {
            console.error(err);
        }
    };

    const handleStatusUpdate = async (id, status) => {
        try {
            await API.put(`/loans/${id}/status?status=${status}`);
            await fetchLoans();
        } catch (err) {
            console.error(err);
        }
    };

    return (
        <div className="p-6 text-white min-h-screen">
            <h1 className="text-2xl font-bold mb-6">
                {isOfficer ? 'Loan Management (Officer)' : 'Apply Loans'}
            </h1>

            {/* CUSTOMER ONLY FORM */}
            {!isOfficer && (
                <div className="bg-slate-900 border border-slate-800 rounded-xl p-6 mb-8 max-w-2xl shadow-lg">
                    <h2 className="text-lg font-semibold mb-4">Request New Loan</h2>
                    <form onSubmit={(e) => { void handleSubmit(e); }} className="space-y-4">
                        <select name="loanType" value={formData.loanType} onChange={handleChange} className="w-full bg-slate-800 p-2.5 rounded text-white">
                            <option value="Personal Loan">Personal Loan</option>
                            <option value="Home Loan">Home Loan</option>
                        </select>
                        <input type="number" name="amount" value={formData.amount} onChange={handleChange} placeholder="Amount" className="w-full bg-slate-800 p-2.5 rounded text-white" required />
                        <input type="number" name="durationMonths" value={formData.durationMonths} onChange={handleChange} placeholder="Duration" className="w-full bg-slate-800 p-2.5 rounded text-white" required />
                        <button type="submit" className="w-full bg-blue-600 p-2.5 rounded text-white">Submit</button>
                    </form>
                </div>
            )}

            {/* TABLE */}
            <div className="bg-slate-900 border border-slate-800 rounded-xl p-6 shadow-lg">
                <table className="w-full text-left">
                    <thead>
                    <tr className="border-b border-slate-800 text-slate-400">
                        <th className="pb-3">Loan ID</th>
                        {isOfficer && <th className="pb-3">User ID</th>}
                        <th className="pb-3">Type</th>
                        <th className="pb-3">Amount</th>
                        <th className="pb-3">Status</th>
                        {isOfficer && <th className="pb-3">Action</th>}
                    </tr>
                    </thead>
                    <tbody>
                    {loans.map((loan) => (
                        <tr key={loan.id} className="border-b border-slate-800/50">
                            <td className="py-3">#{loan.id}</td>
                            {isOfficer && <td className="py-3">User #{loan.userId}</td>}
                            <td className="py-3">{loan.loanType}</td>
                            <td className="py-3">${loan.amount}</td>
                            <td className="py-3">{loan.status}</td>
                            {isOfficer && (
                                <td className="py-3 space-x-2">
                                    <button onClick={() => { void handleStatusUpdate(loan.id, 'Approved'); }} className="bg-emerald-600 px-3 py-1 rounded text-xs">Approve</button>
                                    <button onClick={() => { void handleStatusUpdate(loan.id, 'Rejected'); }} className="bg-rose-600 px-3 py-1 rounded text-xs">Reject</button>
                                </td>
                            )}
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
};

export default LoanManagement;