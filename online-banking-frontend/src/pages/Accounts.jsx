import React, { useState } from 'react';
import { CreditCard, Plus, Search, X } from 'lucide-react';

export default function Accounts() {
    const [showModal, setShowModal] = useState(false);
    const [accountType, setAccountType] = useState('Savings');
    const [balance, setBalance] = useState('');

    const [accounts, setAccounts] = useState([
        { id: 1, accNumber: "ACC-1001", type: "Savings", balance: "150,000.00", status: "Active" },
        { id: 2, accNumber: "ACC-1002", type: "Checking", balance: "100,000.00", status: "Active" },
    ]);

    const handleSubmit = (e) => {
        e.preventDefault();
        const newAcc = {
            id: accounts.length + 1,
            accNumber: `ACC-100${accounts.length + 1}`,
            type: accountType,
            balance: parseFloat(balance).toFixed(2),
            status: "Active"
        };
        setAccounts([...accounts, newAcc]);
        setShowModal(false);
        setBalance('');
    };

    return (
        <div className="p-6">
            <div className="flex justify-between items-center mb-6">
                <div>
                    <h1 className="text-2xl font-bold text-gray-800">Bank Accounts</h1>
                    <p className="text-gray-500 text-sm">Manage customer accounts and balances</p>
                </div>
                <button
                    onClick={() => setShowModal(true)}
                    className="flex items-center gap-2 bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg font-medium shadow-md transition cursor-pointer">
                    <Plus size={18} /> Add New Account
                </button>
            </div>

            <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
                <div className="p-4 border-b border-gray-100 flex items-center gap-3">
                    <Search className="text-gray-400" size={20} />
                    <input
                        type="text"
                        placeholder="Search account number..."
                        className="w-full bg-transparent outline-none text-gray-700"
                    />
                </div>

                <table className="w-full text-left border-collapse">
                    <thead>
                    <tr className="bg-gray-50 text-gray-600 text-sm">
                        <th className="p-4">Account Number</th>
                        <th className="p-4">Type</th>
                        <th className="p-4">Balance (LKR)</th>
                        <th className="p-4">Status</th>
                    </tr>
                    </thead>
                    <tbody className="divide-y divide-gray-100">
                    {accounts.map((acc) => (
                        <tr key={acc.id} className="hover:bg-gray-50/50">
                            <td className="p-4 font-medium flex items-center gap-2">
                                <CreditCard className="text-blue-500" size={18} />
                                {acc.accNumber}
                            </td>
                            <td className="p-4 text-gray-600">{acc.type}</td>
                            <td className="p-4 font-semibold text-gray-800">Rs. {acc.balance}</td>
                            <td className="p-4">
                  <span className="px-3 py-1 text-xs font-semibold rounded-full bg-emerald-100 text-emerald-700">
                    {acc.status}
                  </span>
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>

            {showModal && (
                <div className="fixed inset-0 bg-black/50 flex justify-center items-center">
                    <div className="bg-white p-6 rounded-xl w-96 shadow-xl relative">
                        <button
                            onClick={() => setShowModal(false)}
                            className="absolute top-4 right-4 text-gray-400 hover:text-gray-600 cursor-pointer">
                            <X size={20} />
                        </button>
                        <h2 className="text-xl font-bold mb-4 text-gray-800">Create New Account</h2>

                        <form onSubmit={handleSubmit} className="space-y-4">
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">Account Type</label>
                                <select
                                    value={accountType}
                                    onChange={(e) => setAccountType(e.target.value)}
                                    className="w-full border border-gray-300 p-2 rounded-lg outline-none focus:border-blue-500">
                                    <option value="Savings">Savings Account</option>
                                    <option value="Checking">Checking Account</option>
                                </select>
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">Initial Deposit Amount (LKR)</label>
                                <input
                                    type="number"
                                    required
                                    placeholder="e.g. 5000"
                                    value={balance}
                                    onChange={(e) => setBalance(e.target.value)}
                                    className="w-full border border-gray-300 p-2 rounded-lg outline-none focus:border-blue-500"
                                />
                            </div>

                            <button
                                type="submit"
                                className="w-full bg-blue-600 hover:bg-blue-700 text-white py-2 rounded-lg font-medium transition cursor-pointer">
                                Create Account
                            </button>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
}