import { useState } from 'react';
import { ArrowUpRight, ArrowDownLeft, Send, ShieldAlert, CreditCard, DollarSign } from 'lucide-react';

export default function CustomerDashboard({ user }) {
    const [balance, setBalance] = useState(458500);
    const [transferAcc, setTransferAcc] = useState('');
    const [transferAmount, setTransferAmount] = useState('');

    const [transactions, setTransactions] = useState([
        { id: 'TXN9812', name: 'Supermarket POS', type: 'Debit', amount: 'LKR 8,450', date: '2026-08-08' },
        { id: 'TXN9811', name: 'Salary Credit', type: 'Credit', amount: 'LKR 180,000', date: '2026-08-01' },
        { id: 'TXN9810', name: 'CEB Electricity Bill', type: 'Debit', amount: 'LKR 4,200', date: '2026-07-28' },
    ]);

    const handleTransfer = (e) => {
        e.preventDefault();
        const amt = parseFloat(transferAmount);
        if (amt > balance) {
            alert('Insufficient Balance!');
            return;
        }
        setBalance(balance - amt);
        const newTxn = {
            id: `TXN${Math.floor(1000 + Math.random() * 9000)}`,
            name: `Transfer to ${transferAcc}`,
            type: 'Debit',
            amount: `LKR ${amt.toLocaleString()}`,
            date: new Date().toISOString().split('T')[0]
        };
        setTransactions([newTxn, ...transactions]);
        alert(`Successfully transferred LKR ${amt.toLocaleString()} to Account ${transferAcc}`);
        setTransferAcc('');
        setTransferAmount('');
    };

    return (
        <div className="space-y-8">
            <div>
                <h1 className="text-3xl font-extrabold text-white">
                    Welcome back, <span className="text-blue-400">{user?.username || 'Customer'}</span> 👋
                </h1>
                <p className="text-slate-400 text-sm mt-1">APEX Online Banking Digital Account Dashboard</p>
            </div>

            {/* Balance Card & Quick Transfer */}
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">

                {/* Account Balance Widget */}
                <div className="lg:col-span-1 bg-gradient-to-br from-blue-900 via-indigo-950 to-slate-950 p-6 rounded-3xl border border-blue-500/30 shadow-2xl relative overflow-hidden flex flex-col justify-between">
                    <div className="absolute top-0 right-0 w-48 h-48 bg-blue-500/10 rounded-full blur-3xl"></div>
                    <div>
                        <div className="flex justify-between items-center text-xs font-bold uppercase tracking-wider text-blue-300">
                            <span>Savings Account</span>
                            <span className="bg-emerald-500/20 text-emerald-400 px-2 py-0.5 rounded-full border border-emerald-500/30">Active</span>
                        </div>
                        <p className="text-slate-400 text-xs mt-4">Account Number</p>
                        <p className="text-lg font-mono font-bold text-white tracking-widest mt-0.5">8004 - 1289 - 9920</p>

                        <p className="text-slate-400 text-xs mt-6">Available Balance</p>
                        <h2 className="text-3xl font-black text-white mt-1">LKR {balance.toLocaleString()}</h2>
                    </div>

                    <div className="mt-8 pt-4 border-t border-slate-800/80 flex items-center justify-between text-xs text-slate-400">
                        <span>Branch: Colombo Main</span>
                        <span>Currency: LKR</span>
                    </div>
                </div>

                {/* Quick Fund Transfer Form */}
                <div className="lg:col-span-2 bg-slate-950 p-6 rounded-3xl border border-slate-800 shadow-xl space-y-4">
                    <h2 className="text-xl font-bold text-white flex items-center gap-2">
                        <Send size={20} className="text-blue-400" /> Quick Fund Transfer
                    </h2>

                    <form onSubmit={handleTransfer} className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        <div>
                            <label className="block text-slate-400 text-xs font-semibold mb-1 uppercase">Recipient Account No</label>
                            <input
                                type="text"
                                required
                                placeholder="e.g. 8001234567"
                                value={transferAcc}
                                onChange={(e) => setTransferAcc(e.target.value)}
                                className="w-full bg-slate-900 border border-slate-700 text-white p-3 rounded-xl outline-none focus:border-blue-500 transition"
                            />
                        </div>

                        <div>
                            <label className="block text-slate-400 text-xs font-semibold mb-1 uppercase">Amount (LKR)</label>
                            <input
                                type="number"
                                required
                                min="100"
                                placeholder="e.g. 5000"
                                value={transferAmount}
                                onChange={(e) => setTransferAmount(e.target.value)}
                                className="w-full bg-slate-900 border border-slate-700 text-white p-3 rounded-xl outline-none focus:border-blue-500 transition"
                            />
                        </div>

                        <button type="submit" className="md:col-span-2 bg-blue-600 hover:bg-blue-500 text-white py-3 rounded-xl font-bold transition-all shadow-lg hover:shadow-blue-500/25 flex items-center justify-center gap-2 cursor-pointer mt-2">
                            <Send size={18} /> Transfer Funds Instantly
                        </button>
                    </form>
                </div>
            </div>

            {/* Recent Transactions Table */}
            <div className="bg-slate-950 p-6 rounded-3xl border border-slate-800 shadow-xl space-y-4">
                <h2 className="text-xl font-bold text-white flex items-center gap-2">
                    <DollarSign size={20} className="text-blue-400" /> Recent Transactions
                </h2>

                <div className="overflow-x-auto">
                    <table className="w-full text-left text-slate-300">
                        <thead className="bg-slate-900 text-slate-400 text-xs uppercase border-b border-slate-800">
                        <tr>
                            <th className="p-3">Txn ID</th>
                            <th className="p-3">Description</th>
                            <th className="p-3">Date</th>
                            <th className="p-3">Type</th>
                            <th className="p-3 text-right">Amount</th>
                        </tr>
                        </thead>
                        <tbody className="divide-y divide-slate-800 text-sm">
                        {transactions.map((tx) => (
                            <tr key={tx.id} className="hover:bg-slate-900/50 transition">
                                <td className="p-3 font-mono text-xs text-slate-400">{tx.id}</td>
                                <td className="p-3 font-semibold text-white">{tx.name}</td>
                                <td className="p-3 text-slate-400">{tx.date}</td>
                                <td className="p-3">
                    <span className={`inline-flex items-center gap-1 text-xs font-bold ${tx.type === 'Credit' ? 'text-emerald-400' : 'text-rose-400'}`}>
                      {tx.type === 'Credit' ? <ArrowDownLeft size={14} /> : <ArrowUpRight size={14} />}
                        {tx.type}
                    </span>
                                </td>
                                <td className={`p-3 text-right font-bold ${tx.type === 'Credit' ? 'text-emerald-400' : 'text-slate-200'}`}>
                                    {tx.type === 'Credit' ? `+ ${tx.amount}` : `- ${tx.amount}`}
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