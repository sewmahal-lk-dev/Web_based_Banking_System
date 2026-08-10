import { useState } from 'react';
import { TrendingUp, Plus, Eye, Check, X, DollarSign, Receipt, Trash2, ArrowUpRight, ShieldCheck } from 'lucide-react';

export default function Investments({ role }) {
    const isOfficer = role === 'INVESTMENT_OFFICER' || role === 'ADMIN';

    // Navigation Tabs
    const [activeTab, setActiveTab] = useState('investments'); // 'investments' | 'payments'

    // Modal & Profile States
    const [selectedUser, setSelectedUser] = useState(null);
    const [activeApproveItem, setActiveApproveItem] = useState(null);

    // ------------------- INVESTMENTS STATE (CRUD) -------------------
    const [investments, setInvestments] = useState([
        { id: 'INV-101', applicant: 'Pathum', plan: 'Fixed Deposit (12 Months)', amount: 1000000, rate: 11.5, status: 'Active', startDate: '2025-01-15' },
        { id: 'INV-102', applicant: 'Kasun Perera', plan: 'Treasury Bills Vault', amount: 500000, rate: 9.8, status: 'Pending Approval', startDate: '2025-02-10' }
    ]);

    // Investment Form State
    const [invPlan, setInvPlan] = useState('Fixed Deposit (12 Months - 11.5%)');
    const [invAmount, setInvAmount] = useState('100000');

    // ------------------- BILL PAYMENTS STATE (CRUD) -------------------
    const [payments, setPayments] = useState([
        { id: 'PAY-801', applicant: 'Pathum', biller: 'CEB Electricity', refNo: '448291039', amount: 8500, status: 'Completed', date: '2025-02-12' },
        { id: 'PAY-802', applicant: 'Kasun Perera', biller: 'Water Board', refNo: '109283746', amount: 3200, status: 'Pending', date: '2025-02-14' }
    ]);

    // Payment Form State
    const [biller, setBiller] = useState('CEB Electricity');
    const [accountRef, setAccountRef] = useState('');
    const [payAmount, setPayAmount] = useState('');

    // ---------- INVESTMENT HANDLERS ----------
    const handleCreateInvestment = (e) => {
        e.preventDefault();
        let rate = 10.0;
        if (invPlan.includes('11.5%')) rate = 11.5;
        if (invPlan.includes('9.8%')) rate = 9.8;
        if (invPlan.includes('13.0%')) rate = 13.0;

        const newInv = {
            id: `INV-${Math.floor(100 + Math.random() * 900)}`,
            applicant: 'Logged Customer',
            plan: invPlan.split(' - ')[0],
            amount: Number(invAmount),
            rate: rate,
            status: 'Pending Approval',
            startDate: new Date().toISOString().split('T')[0]
        };

        setInvestments([newInv, ...investments]);
        setInvAmount('');
        alert('Investment Application Submitted Successfully!');
    };

    const handleCloseInvestment = (id) => {
        if (window.confirm('Are you sure you want to withdraw and close this investment plan?')) {
            setInvestments(investments.filter(inv => inv.id !== id));
            alert('Investment Closed Successfully!');
        }
    };

    // ---------- PAYMENT HANDLERS ----------
    const handleCreatePayment = (e) => {
        e.preventDefault();
        const newPay = {
            id: `PAY-${Math.floor(800 + Math.random() * 100)}`,
            applicant: 'Logged Customer',
            biller: biller,
            refNo: accountRef,
            amount: Number(payAmount),
            status: 'Pending',
            date: new Date().toISOString().split('T')[0]
        };

        setPayments([newPay, ...payments]);
        setAccountRef('');
        setPayAmount('');
        alert('Bill Payment Initiated Successfully!');
    };

    const handleCancelPayment = (id) => {
        if (window.confirm('Are you sure you want to cancel this pending payment?')) {
            setPayments(payments.filter(p => p.id !== id));
        }
    };

    // ---------- OFFICER APPROVAL HANDLERS ----------
    const handleApproveStatus = (type) => {
        if (type === 'INV') {
            setInvestments(investments.map(i => i.id === activeApproveItem.id ? { ...i, status: 'Active' } : i));
        } else {
            setPayments(payments.map(p => p.id === activeApproveItem.id ? { ...p, status: 'Completed' } : p));
        }
        setActiveApproveItem(null);
    };

    const handleRejectStatus = (id, type) => {
        if (type === 'INV') {
            setInvestments(investments.map(i => i.id === id ? { ...i, status: 'Rejected' } : i));
        } else {
            setPayments(payments.map(p => p.id === id ? { ...p, status: 'Failed' } : p));
        }
    };

    return (
        <div className="space-y-8">
            {/* HEADER & TOP NAVIGATION TABS */}
            <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
                <div>
                    <h1 className="text-3xl font-extrabold text-white flex items-center gap-3">
                        <TrendingUp className="text-emerald-400" size={32} />
                        Investment & Payment Management
                    </h1>
                    <p className="text-slate-400 text-sm mt-1">
                        Department Officer: <span className="text-emerald-400 font-semibold">Gunarathna W.</span> | Role: <span className="text-cyan-400 font-semibold">{role}</span>
                    </p>
                </div>

                {/* TABS SWITCHER */}
                <div className="bg-slate-900 p-1.5 rounded-2xl border border-slate-800 flex gap-1">
                    <button
                        onClick={() => setActiveTab('investments')}
                        className={`px-5 py-2.5 rounded-xl font-bold text-xs transition flex items-center gap-2 cursor-pointer ${
                            activeTab === 'investments' ? 'bg-emerald-600 text-white shadow-lg' : 'text-slate-400 hover:text-white'
                        }`}
                    >
                        <DollarSign size={16} /> Investment Plans
                    </button>
                    <button
                        onClick={() => setActiveTab('payments')}
                        className={`px-5 py-2.5 rounded-xl font-bold text-xs transition flex items-center gap-2 cursor-pointer ${
                            activeTab === 'payments' ? 'bg-blue-600 text-white shadow-lg' : 'text-slate-400 hover:text-white'
                        }`}
                    >
                        <Receipt size={16} /> Bill Payments
                    </button>
                </div>
            </div>

            {/* ==================== SECTION 1: INVESTMENT PLANS ==================== */}
            {activeTab === 'investments' && (
                <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
                    {/* CREATE INVESTMENT FORM (CUSTOMER ONLY) */}
                    {!isOfficer && (
                        <div className="bg-slate-950 p-6 rounded-2xl border border-slate-800 shadow-xl space-y-4">
                            <h2 className="text-xl font-bold text-white flex items-center gap-2">
                                <Plus size={20} className="text-emerald-400" /> Create Investment
                            </h2>
                            <form onSubmit={handleCreateInvestment} className="space-y-4">
                                <div>
                                    <label className="block text-slate-400 text-xs font-semibold mb-1 uppercase">Select Investment Plan</label>
                                    <select value={invPlan} onChange={(e) => setInvPlan(e.target.value)} className="w-full bg-slate-900 border border-slate-700 text-white rounded-xl p-3 outline-none cursor-pointer">
                                        <option value="Fixed Deposit (12 Months - 11.5%)">Fixed Deposit (12 Months - 11.5%)</option>
                                        <option value="Treasury Bills Vault - 9.8%">Treasury Bills Vault - 9.8%</option>
                                        <option value="High Growth Mutual Fund - 13.0%">High Growth Mutual Fund - 13.0%</option>
                                    </select>
                                </div>

                                <div>
                                    <label className="block text-slate-400 text-xs font-semibold mb-1 uppercase">Investment Amount (LKR)</label>
                                    <input type="number" required min="10000" placeholder="e.g. 100000" value={invAmount} onChange={(e) => setInvAmount(e.target.value)} className="w-full bg-slate-900 border border-slate-700 text-white p-3 rounded-xl outline-none" />
                                </div>

                                <div className="bg-slate-900/60 p-3 rounded-xl border border-slate-800 text-xs space-y-1 text-slate-300">
                                    <p className="flex justify-between"><span>Est. Annual Return:</span> <strong className="text-emerald-400">{invPlan.includes('11.5%') ? '11.5%' : invPlan.includes('13.0%') ? '13.0%' : '9.8%'}</strong></p>
                                    <p className="flex justify-between"><span>Est. Maturity Profit:</span> <strong className="text-cyan-400">LKR {((Number(invAmount || 0) * (invPlan.includes('11.5%') ? 0.115 : 0.098))).toLocaleString()}</strong></p>
                                </div>

                                <button type="submit" className="w-full bg-emerald-600 hover:bg-emerald-500 text-white py-3 rounded-xl font-bold transition cursor-pointer">
                                    Submit Investment Application
                                </button>
                            </form>
                        </div>
                    )}

                    {/* ACTIVE INVESTMENTS TABLE */}
                    <div className={!isOfficer ? 'lg:col-span-2' : 'lg:col-span-3'}>
                        <div className="bg-slate-950 p-6 rounded-2xl border border-slate-800 shadow-xl space-y-4">
                            <h2 className="text-xl font-bold text-white flex items-center justify-between">
                                <span>{isOfficer ? 'Officer Queue: Investment Applications' : 'Track Returns & Portfolios'}</span>
                                <span className="text-xs text-slate-400">{investments.length} Active Plans</span>
                            </h2>

                            <div className="overflow-x-auto">
                                <table className="w-full text-left text-slate-300">
                                    <thead className="bg-slate-900 text-slate-400 text-xs uppercase border-b border-slate-800">
                                    <tr>
                                        <th className="p-3">Plan / Investor</th>
                                        <th className="p-3">Amount</th>
                                        <th className="p-3">Interest Rate</th>
                                        <th className="p-3">Est. Return</th>
                                        <th className="p-3">Status</th>
                                        <th className="p-3 text-right">Actions</th>
                                    </tr>
                                    </thead>
                                    <tbody className="divide-y divide-slate-800 text-sm">
                                    {investments.map((inv) => {
                                        const annualProfit = (inv.amount * (inv.rate / 100)).toLocaleString();
                                        return (
                                            <tr key={inv.id} className="hover:bg-slate-900/50">
                                                <td className="p-3">
                                                    <p className="font-bold text-white">{inv.plan}</p>
                                                    <button
                                                        onClick={() => setSelectedUser({ fullName: inv.applicant, accNo: '8004-9920-1122', email: 'investor@apex.lk' })}
                                                        className="text-xs text-blue-400 hover:underline flex items-center gap-1 font-semibold cursor-pointer">
                                                        <Eye size={12} /> {inv.applicant}
                                                    </button>
                                                </td>
                                                <td className="p-3 font-mono font-bold text-white">LKR {inv.amount.toLocaleString()}</td>
                                                <td className="p-3 font-bold text-emerald-400">{inv.rate}% p.a.</td>
                                                <td className="p-3 text-xs text-cyan-300 font-mono">+LKR {annualProfit}</td>
                                                <td className="p-3">
                            <span className={`px-2.5 py-1 rounded-full text-xs font-bold ${
                                inv.status === 'Active' ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20' :
                                    inv.status === 'Rejected' ? 'bg-rose-500/10 text-rose-400' : 'bg-amber-500/10 text-amber-400'
                            }`}>
                              {inv.status}
                            </span>
                                                </td>
                                                <td className="p-3 text-right">
                                                    {isOfficer && inv.status === 'Pending Approval' ? (
                                                        <div className="flex justify-end gap-2">
                                                            <button onClick={() => setActiveApproveItem({ ...inv, itemType: 'INV' })} className="p-1.5 bg-emerald-500/20 text-emerald-400 rounded-lg hover:bg-emerald-500/30 text-xs font-bold flex items-center gap-1 cursor-pointer">
                                                                <Check size={14} /> Approve
                                                            </button>
                                                            <button onClick={() => handleRejectStatus(inv.id, 'INV')} className="p-1.5 bg-rose-500/20 text-rose-400 rounded-lg hover:bg-rose-500/30 text-xs font-bold flex items-center gap-1 cursor-pointer">
                                                                <X size={14} /> Reject
                                                            </button>
                                                        </div>
                                                    ) : (
                                                        !isOfficer && (
                                                            <button onClick={() => handleCloseInvestment(inv.id)} className="p-1.5 bg-rose-500/10 text-rose-400 hover:bg-rose-500/20 rounded-lg text-xs font-semibold flex items-center gap-1 ml-auto cursor-pointer">
                                                                <Trash2 size={13} /> Close
                                                            </button>
                                                        )
                                                    )}
                                                </td>
                                            </tr>
                                        );
                                    })}
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                </div>
            )}

            {/* ==================== SECTION 2: BILL PAYMENTS ==================== */}
            {activeTab === 'payments' && (
                <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
                    {/* CREATE PAYMENT FORM */}
                    {!isOfficer && (
                        <div className="bg-slate-950 p-6 rounded-2xl border border-slate-800 shadow-xl space-y-4">
                            <h2 className="text-xl font-bold text-white flex items-center gap-2">
                                <Plus size={20} className="text-blue-400" /> New Bill Payment
                            </h2>
                            <form onSubmit={handleCreatePayment} className="space-y-4">
                                <div>
                                    <label className="block text-slate-400 text-xs font-semibold mb-1 uppercase">Select Utility Biller</label>
                                    <select value={biller} onChange={(e) => setBiller(e.target.value)} className="w-full bg-slate-900 border border-slate-700 text-white rounded-xl p-3 outline-none cursor-pointer">
                                        <option value="CEB Electricity">CEB Electricity</option>
                                        <option value="Water Supply Board">Water Supply Board</option>
                                        <option value="Dialog Axiata Broadband">Dialog Axiata Broadband</option>
                                        <option value="SLT Mobitel Landline">SLT Mobitel Landline</option>
                                    </select>
                                </div>

                                <div>
                                    <label className="block text-slate-400 text-xs font-semibold mb-1 uppercase">Account / Reference No</label>
                                    <input type="text" required placeholder="e.g. 448201923" value={accountRef} onChange={(e) => setAccountRef(e.target.value)} className="w-full bg-slate-900 border border-slate-700 text-white p-3 rounded-xl outline-none font-mono" />
                                </div>

                                <div>
                                    <label className="block text-slate-400 text-xs font-semibold mb-1 uppercase">Payment Amount (LKR)</label>
                                    <input type="number" required min="100" placeholder="e.g. 5000" value={payAmount} onChange={(e) => setPayAmount(e.target.value)} className="w-full bg-slate-900 border border-slate-700 text-white p-3 rounded-xl outline-none" />
                                </div>

                                <button type="submit" className="w-full bg-blue-600 hover:bg-blue-500 text-white py-3 rounded-xl font-bold transition cursor-pointer">
                                    Pay Bill Now
                                </button>
                            </form>
                        </div>
                    )}

                    {/* BILL PAYMENT HISTORY TABLE */}
                    <div className={!isOfficer ? 'lg:col-span-2' : 'lg:col-span-3'}>
                        <div className="bg-slate-950 p-6 rounded-2xl border border-slate-800 shadow-xl space-y-4">
                            <h2 className="text-xl font-bold text-white flex items-center justify-between">
                                <span>{isOfficer ? 'Officer Payment Settlement Queue' : 'Bill Payment History'}</span>
                                <span className="text-xs text-slate-400">{payments.length} Transactions</span>
                            </h2>

                            <div className="overflow-x-auto">
                                <table className="w-full text-left text-slate-300">
                                    <thead className="bg-slate-900 text-slate-400 text-xs uppercase border-b border-slate-800">
                                    <tr>
                                        <th className="p-3">Biller / Ref No</th>
                                        <th className="p-3">Payer</th>
                                        <th className="p-3">Amount</th>
                                        <th className="p-3">Date</th>
                                        <th className="p-3">Status</th>
                                        <th className="p-3 text-right">Actions</th>
                                    </tr>
                                    </thead>
                                    <tbody className="divide-y divide-slate-800 text-sm">
                                    {payments.map((pay) => (
                                        <tr key={pay.id} className="hover:bg-slate-900/50">
                                            <td className="p-3">
                                                <p className="font-bold text-white">{pay.biller}</p>
                                                <p className="text-xs font-mono text-slate-400">Ref: {pay.refNo}</p>
                                            </td>
                                            <td className="p-3 text-xs text-slate-300">{pay.applicant}</td>
                                            <td className="p-3 font-mono font-bold text-emerald-400">LKR {pay.amount.toLocaleString()}</td>
                                            <td className="p-3 text-xs text-slate-400">{pay.date}</td>
                                            <td className="p-3">
                          <span className={`px-2.5 py-1 rounded-full text-xs font-bold ${
                              pay.status === 'Completed' ? 'bg-emerald-500/10 text-emerald-400' :
                                  pay.status === 'Failed' ? 'bg-rose-500/10 text-rose-400' : 'bg-amber-500/10 text-amber-400'
                          }`}>
                            {pay.status}
                          </span>
                                            </td>
                                            <td className="p-3 text-right">
                                                {isOfficer && pay.status === 'Pending' ? (
                                                    <div className="flex justify-end gap-2">
                                                        <button onClick={() => setActiveApproveItem({ ...pay, itemType: 'PAY' })} className="p-1.5 bg-emerald-500/20 text-emerald-400 rounded-lg hover:bg-emerald-500/30 text-xs font-bold flex items-center gap-1 cursor-pointer">
                                                            <Check size={14} /> Settle
                                                        </button>
                                                        <button onClick={() => handleRejectStatus(pay.id, 'PAY')} className="p-1.5 bg-rose-500/20 text-rose-400 rounded-lg hover:bg-rose-500/30 text-xs font-bold flex items-center gap-1 cursor-pointer">
                                                            <X size={14} /> Reject
                                                        </button>
                                                    </div>
                                                ) : (
                                                    !isOfficer && pay.status === 'Pending' && (
                                                        <button onClick={() => handleCancelPayment(pay.id)} className="p-1.5 bg-rose-500/10 text-rose-400 hover:bg-rose-500/20 rounded-lg text-xs font-semibold flex items-center gap-1 ml-auto cursor-pointer">
                                                            <X size={13} /> Cancel
                                                        </button>
                                                    )
                                                )}
                                            </td>
                                        </tr>
                                    ))}
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                </div>
            )}

            {/* MODAL: APPROVE CONFIRMATION */}
            {activeApproveItem && (
                <div className="fixed inset-0 bg-black/80 backdrop-blur-sm flex items-center justify-center p-4 z-50">
                    <div className="bg-slate-900 border border-slate-700 p-6 rounded-3xl max-w-md w-full space-y-4 shadow-2xl">
                        <h3 className="text-xl font-bold text-white flex items-center gap-2">
                            <ShieldCheck className="text-emerald-400" /> Confirm Approval
                        </h3>
                        <p className="text-xs text-slate-300">
                            Are you sure you want to approve this {activeApproveItem.itemType === 'INV' ? 'Investment Plan' : 'Bill Payment'} request for <strong className="text-white">{activeApproveItem.applicant}</strong>?
                        </p>
                        <div className="bg-slate-950 p-3 rounded-xl border border-slate-800 text-xs font-mono text-emerald-400 space-y-1">
                            <p>ID: {activeApproveItem.id}</p>
                            <p>Amount: LKR {activeApproveItem.amount.toLocaleString()}</p>
                        </div>
                        <div className="flex gap-2 pt-2">
                            <button onClick={() => setActiveApproveItem(null)} className="w-1/2 bg-slate-800 text-white py-2.5 rounded-xl font-bold cursor-pointer">Cancel</button>
                            <button onClick={() => handleApproveStatus(activeApproveItem.itemType)} className="w-1/2 bg-emerald-600 hover:bg-emerald-500 text-white py-2.5 rounded-xl font-bold cursor-pointer">Confirm Approval</button>
                        </div>
                    </div>
                </div>
            )}

            {/* MODAL: USER DETAILS */}
            {selectedUser && (
                <div className="fixed inset-0 bg-black/80 backdrop-blur-sm flex items-center justify-center p-4 z-50">
                    <div className="bg-slate-900 border border-slate-700 p-6 rounded-3xl max-w-md w-full space-y-4 shadow-2xl">
                        <h3 className="text-xl font-bold text-white">{selectedUser.fullName}</h3>
                        <div className="space-y-2 bg-slate-950 p-4 rounded-xl text-xs text-slate-300 border border-slate-800">
                            <p><strong>Primary Account:</strong> {selectedUser.accNo}</p>
                            <p><strong>Email:</strong> {selectedUser.email}</p>
                        </div>
                        <button onClick={() => setSelectedUser(null)} className="w-full bg-slate-800 text-white py-2.5 rounded-xl font-bold cursor-pointer">Close</button>
                    </div>
                </div>
            )}
        </div>
    );
}