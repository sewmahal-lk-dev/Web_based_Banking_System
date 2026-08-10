import { useState } from 'react';
import { HelpCircle, Plus, Eye, User, MessageSquare, Send } from 'lucide-react';

export default function CustomerSupport({ role }) {
    const isOfficer = role === 'SUPPORT_OFFICER' || role === 'ADMIN';

    const customerDatabase = {
        'Pathum': { fullName: 'Pathum M.G.D.D.', nic: '199824500123', phone: '0771234567', email: 'pathum@gmail.com', accNo: '8004-1289-9920' },
        'Kasun Perera': { fullName: 'Kasun Perera', nic: '199211002341', phone: '0719876543', email: 'kasun@gmail.com', accNo: '8001-4455-8812' }
    };

    const [selectedCustomer, setSelectedCustomer] = useState(null);
    const [selectedTicket, setSelectedTicket] = useState(null);
    const [replyText, setReplyText] = useState('');

    const [tickets, setTickets] = useState([
        { id: 'TICK-901', customer: 'Pathum', category: 'Transaction Issue', subject: 'Failed ATM Cash Withdrawal', description: 'Money deducted from account but ATM did not dispense cash.', status: 'In Progress', reply: '' },
        { id: 'TICK-902', customer: 'Kasun Perera', category: 'Card Problem', subject: 'Online Card Payment Declined', description: 'International payment declined on POS transaction.', status: 'Resolved', reply: 'Card international limit enabled from backend.' }
    ]);

    const [category, setCategory] = useState('Transaction Issue');
    const [subject, setSubject] = useState('');
    const [description, setDescription] = useState('');

    const handleCreateTicket = (e) => {
        e.preventDefault();
        const newTicket = {
            id: `TICK-${Math.floor(900 + Math.random() * 100)}`,
            customer: 'Logged Customer',
            category,
            subject,
            description,
            status: 'In Progress',
            reply: ''
        };
        setTickets([newTicket, ...tickets]);
        setSubject('');
        setDescription('');
        alert('Support Ticket Submitted Successfully!');
    };

    const handleResolveTicket = (id) => {
        setTickets(tickets.map(t => t.id === id ? { ...t, status: 'Resolved', reply: replyText || t.reply } : t));
        setSelectedTicket(null);
        setReplyText('');
    };

    return (
        <div className="space-y-8">
            <div>
                <h1 className="text-3xl font-extrabold text-white flex items-center gap-3">
                    <HelpCircle className="text-rose-400" size={32} />
                    Customer Support & Inquiry Portal
                </h1>
                <p className="text-slate-400 text-sm mt-1">
                    Logged in Role: <span className="text-cyan-400 font-semibold">{role}</span>
                </p>
            </div>

            {isOfficer ? (
                /* OFFICER VIEW: TICKETS LIST */
                <div className="bg-slate-950 p-6 rounded-2xl border border-slate-800 shadow-xl space-y-4">
                    <h2 className="text-xl font-bold text-white flex items-center justify-between">
                        <span>Customer Support Tickets Queue</span>
                        <span className="text-xs text-slate-400">{tickets.length} Tickets</span>
                    </h2>

                    <div className="overflow-x-auto">
                        <table className="w-full text-left text-slate-300">
                            <thead className="bg-slate-900 text-slate-400 text-xs uppercase border-b border-slate-800">
                            <tr>
                                <th className="p-3">Ticket ID / Customer</th>
                                <th className="p-3">Category</th>
                                <th className="p-3">Subject</th>
                                <th className="p-3">Status</th>
                                <th className="p-3 text-right">Actions</th>
                            </tr>
                            </thead>
                            <tbody className="divide-y divide-slate-800 text-sm">
                            {tickets.map((t) => (
                                <tr key={t.id} className="hover:bg-slate-900/50">
                                    <td className="p-3">
                                        <p className="font-bold text-rose-400">{t.id}</p>
                                        <button
                                            onClick={() => setSelectedCustomer(customerDatabase[t.customer] || { fullName: t.customer, nic: '199800000000', phone: '0770000000', email: 'cust@apex.lk', accNo: '8000-0000-0000' })}
                                            className="text-xs text-cyan-400 hover:underline flex items-center gap-1 cursor-pointer">
                                            <Eye size={12} /> {t.customer}
                                        </button>
                                    </td>
                                    <td className="p-3">{t.category}</td>
                                    <td className="p-3">
                                        <p className="font-bold text-white">{t.subject}</p>
                                        <p className="text-xs text-slate-400 truncate max-w-xs">{t.description}</p>
                                    </td>
                                    <td className="p-3">
                      <span className={`px-2.5 py-1 rounded-full text-xs font-bold ${t.status === 'Resolved' ? 'bg-emerald-500/10 text-emerald-400' : 'bg-amber-500/10 text-amber-400'}`}>
                        {t.status}
                      </span>
                                    </td>
                                    <td className="p-3 text-right">
                                        {t.status !== 'Resolved' && (
                                            <button onClick={() => setSelectedTicket(t)} className="px-3 py-1.5 bg-rose-600 hover:bg-rose-500 text-white rounded-lg text-xs font-bold flex items-center gap-1 ml-auto cursor-pointer">
                                                <MessageSquare size={14} /> Respond
                                            </button>
                                        )}
                                    </td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                    </div>
                </div>
            ) : (
                /* CUSTOMER VIEW: CREATE TICKET FORM */
                <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
                    <div className="bg-slate-950 p-6 rounded-2xl border border-slate-800 shadow-xl space-y-4">
                        <h2 className="text-xl font-bold text-white flex items-center gap-2">
                            <Plus size={20} className="text-rose-400" /> Raise Support Ticket
                        </h2>
                        <form onSubmit={handleCreateTicket} className="space-y-4">
                            <div>
                                <label className="block text-slate-400 text-xs font-semibold mb-1 uppercase">Inquiry Category</label>
                                <select value={category} onChange={(e) => setCategory(e.target.value)} className="w-full bg-slate-900 border border-slate-700 text-white rounded-xl p-3 outline-none">
                                    <option value="Transaction Issue">Transaction Issue</option>
                                    <option value="Card Problem">Card Problem</option>
                                    <option value="Loan Inquiry">Loan Inquiry</option>
                                    <option value="Online Banking">Online Banking</option>
                                </select>
                            </div>
                            <div>
                                <label className="block text-slate-400 text-xs font-semibold mb-1 uppercase">Subject</label>
                                <input type="text" required placeholder="Brief summary" value={subject} onChange={(e) => setSubject(e.target.value)} className="w-full bg-slate-900 border border-slate-700 text-white p-3 rounded-xl outline-none" />
                            </div>
                            <div>
                                <label className="block text-slate-400 text-xs font-semibold mb-1 uppercase">Detailed Description</label>
                                <textarea required rows="4" placeholder="Provide full details..." value={description} onChange={(e) => setDescription(e.target.value)} className="w-full bg-slate-900 border border-slate-700 text-white p-3 rounded-xl outline-none" />
                            </div>
                            <button type="submit" className="w-full bg-rose-600 hover:bg-rose-500 text-white py-3 rounded-xl font-bold transition flex items-center justify-center gap-2 cursor-pointer">
                                <Send size={16} /> Submit Inquiry
                            </button>
                        </form>
                    </div>

                    <div className="lg:col-span-2 bg-slate-950 p-6 rounded-2xl border border-slate-800 shadow-xl space-y-4">
                        <h2 className="text-xl font-bold text-white">Recent Support History</h2>
                        <div className="space-y-3">
                            {tickets.map(t => (
                                <div key={t.id} className="p-4 bg-slate-900 border border-slate-800 rounded-xl space-y-2">
                                    <div className="flex justify-between items-center">
                                        <span className="font-mono text-xs text-rose-400 font-bold">{t.id} • {t.category}</span>
                                        <span className={`px-2 py-0.5 rounded-full text-xs font-bold ${t.status === 'Resolved' ? 'bg-emerald-500/10 text-emerald-400' : 'bg-amber-500/10 text-amber-400'}`}>{t.status}</span>
                                    </div>
                                    <h4 className="font-bold text-white">{t.subject}</h4>
                                    <p className="text-slate-400 text-xs">{t.description}</p>
                                    {t.reply && <div className="p-3 bg-slate-950 rounded-lg text-xs text-emerald-400 border border-slate-800"><strong>Officer Reply:</strong> {t.reply}</div>}
                                </div>
                            ))}
                        </div>
                    </div>
                </div>
            )}

            {/* RESPOND MODAL */}
            {selectedTicket && (
                <div className="fixed inset-0 bg-black/80 backdrop-blur-sm flex items-center justify-center p-4 z-50">
                    <div className="bg-slate-900 border border-slate-700 p-6 rounded-3xl max-w-md w-full space-y-4 shadow-2xl">
                        <h3 className="text-xl font-bold text-white">Resolve Ticket: {selectedTicket.id}</h3>
                        <textarea rows="4" placeholder="Enter resolution response..." value={replyText} onChange={(e) => setReplyText(e.target.value)} className="w-full bg-slate-950 border border-slate-800 text-white p-3 rounded-xl outline-none text-sm" />
                        <div className="flex gap-2">
                            <button onClick={() => setSelectedTicket(null)} className="w-1/2 bg-slate-800 text-white py-2.5 rounded-xl font-bold">Cancel</button>
                            <button onClick={() => handleResolveTicket(selectedTicket.id)} className="w-1/2 bg-rose-600 hover:bg-rose-500 text-white py-2.5 rounded-xl font-bold">Mark Resolved</button>
                        </div>
                    </div>
                </div>
            )}

            {/* KYC PROFILE MODAL */}
            {selectedCustomer && (
                <div className="fixed inset-0 bg-black/80 backdrop-blur-sm flex items-center justify-center p-4 z-50">
                    <div className="bg-slate-900 border border-slate-700 p-6 rounded-3xl max-w-md w-full space-y-4 shadow-2xl relative">
                        <h3 className="text-xl font-bold text-white flex items-center gap-2"><User className="text-cyan-400" /> {selectedCustomer.fullName}</h3>
                        <div className="space-y-2 bg-slate-950 p-4 rounded-xl text-xs text-slate-300">
                            <p><strong>NIC:</strong> {selectedCustomer.nic}</p>
                            <p><strong>Account:</strong> {selectedCustomer.accNo}</p>
                            <p><strong>Phone:</strong> {selectedCustomer.phone}</p>
                            <p><strong>Email:</strong> {selectedCustomer.email}</p>
                        </div>
                        <button onClick={() => setSelectedCustomer(null)} className="w-full bg-slate-800 text-white py-2.5 rounded-xl font-bold">Close</button>
                    </div>
                </div>
            )}
        </div>
    );
}