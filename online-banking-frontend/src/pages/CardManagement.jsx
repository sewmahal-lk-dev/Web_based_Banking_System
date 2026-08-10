import { useState, useEffect, useCallback } from 'react';
import API from '../api';
import { CreditCard, Plus, Eye, EyeOff, Lock, Unlock, ShieldAlert, AlertTriangle, Info } from 'lucide-react';

export default function CardManagement({ role = 'CARD_OFFICER' }) {
    const isOfficer = role === 'CARD_OFFICER' || role === 'ADMIN';
    const currentCustomer = 'Pathum';

    const [cards, setCards] = useState([]);
    const [newCardType, setNewCardType] = useState('VISA Credit');
    const [newLimit, setNewLimit] = useState('100000');
    const [applicantName, setApplicantName] = useState(currentCustomer);

    // Modals
    const [selectedProfile, setSelectedProfile] = useState(null);
    const [showFullCard, setShowFullCard] = useState(false);
    const [blockModalCard, setBlockModalCard] = useState(null);
    const [selectedReason, setSelectedReason] = useState('Lost or Stolen Card');
    const [customReason, setCustomReason] = useState('');

    // Fetch Cards - ESLint Promise & Hooks Rules Compliant
    const fetchCards = useCallback(() => {
        API.get('/cards')
            .then((res) => {
                setCards(res.data);
            })
            .catch((error) => {
                console.error("Database connection failed:", error);
            });
    }, []);

    useEffect(() => {
        fetchCards();
    }, [fetchCards]);

    const displayedCards = isOfficer
        ? cards
        : cards.filter(c => c.holder && c.holder.toLowerCase() === currentCustomer.toLowerCase());

    const maskCardNumber = (num) => {
        if (!num) return '•••• •••• •••• 0000';
        const parts = num.split(' ');
        return `•••• •••• •••• ${parts[3] || '0000'}`;
    };

    // APPLY / ISSUE CARD
    const handleApplyCard = (e) => {
        e.preventDefault();
        const prefix = newCardType.includes('VISA') ? '4532' : '5412';
        const generatedFullNo = `${prefix} ${Math.floor(1000 + Math.random() * 9000)} ${Math.floor(1000 + Math.random() * 9000)} ${Math.floor(1000 + Math.random() * 9000)}`;

        const newCard = {
            holder: isOfficer ? applicantName : currentCustomer,
            cardType: newCardType,
            fullCardNumber: generatedFullNo,
            cvv: Math.floor(100 + Math.random() * 900).toString(),
            expiry: '12/30',
            status: isOfficer ? 'Active' : 'Pending Approval',
            pendingReq: isOfficer ? 'None' : 'New Card Request',
            blockReason: '',
            limitAmount: Number(newLimit)
        };

        API.post('/cards', newCard)
            .then(() => {
                alert(isOfficer ? 'Card Issued Successfully!' : 'Card Application Submitted!');
                setNewLimit('100000');
                fetchCards();
            })
            .catch((error) => {
                console.error("Error issuing card:", error);
                alert('Failed to issue card!');
            });
    };

    // CUSTOMER: Block Request Submit
    const submitBlockRequest = () => {
        const finalReason = selectedReason === 'Other' ? customReason : selectedReason;

        API.put(`/cards/${blockModalCard.id}/block-request`, {
            blockReason: finalReason
        })
            .then(() => {
                alert('Block Request & Reason submitted to Database!');
                setBlockModalCard(null);
                setCustomReason('');
                fetchCards();
            })
            .catch((error) => {
                console.error("Error submitting block request:", error);
                alert('Failed to submit request!');
            });
    };

    // CUSTOMER: Request Unblock
    const handleUnblockRequest = (id) => {
        API.put(`/cards/${id}/unblock-request`)
            .then(() => {
                alert('Unblock Request sent to Card Officer!');
                fetchCards();
            })
            .catch((error) => {
                console.error("Error sending unblock request:", error);
                alert('Failed to send request!');
            });
    };

    // OFFICER: Toggle Block/Unblock
    const handleToggleBlock = (id) => {
        API.put(`/cards/${id}/toggle-block`)
            .then(() => {
                fetchCards();
            })
            .catch((error) => {
                console.error("Error toggling block:", error);
                alert('Action failed!');
            });
    };

    return (
        <div className="space-y-8">
            <div>
                <h1 className="text-3xl font-extrabold text-white flex items-center gap-3">
                    <CreditCard className="text-purple-400" size={32} />
                    {isOfficer ? 'Credit & Debit Card Management' : 'My Cards & Application Portal'}
                </h1>
                <p className="text-slate-400 text-sm mt-1">
                    Department Officer Management System | Logged in as: <span className="text-cyan-400 font-semibold">{role}</span>
                </p>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
                {/* APPLY FORM */}
                <div className="bg-slate-950 p-6 rounded-2xl border border-slate-800 shadow-xl space-y-4">
                    <h2 className="text-xl font-bold text-white flex items-center gap-2">
                        <Plus size={20} className="text-purple-400" /> Apply for New Card
                    </h2>
                    <form onSubmit={handleApplyCard} className="space-y-4">
                        {isOfficer && (
                            <div>
                                <label className="block text-slate-400 text-xs font-semibold mb-1 uppercase">Applicant Name</label>
                                <input type="text" required value={applicantName} onChange={(e) => setApplicantName(e.target.value)} className="w-full bg-slate-900 border border-slate-700 text-white p-3 rounded-xl outline-none text-sm" />
                            </div>
                        )}
                        <div>
                            <label className="block text-slate-400 text-xs font-semibold mb-1 uppercase">Select Card Type</label>
                            <select value={newCardType} onChange={(e) => setNewCardType(e.target.value)} className="w-full bg-slate-900 border border-slate-700 text-white rounded-xl p-3 outline-none text-sm cursor-pointer">
                                <option value="VISA Credit">VISA Platinum Credit</option>
                                <option value="MasterCard Credit">MasterCard World Credit</option>
                                <option value="VISA Debit">VISA Classic Debit</option>
                            </select>
                        </div>
                        <div>
                            <label className="block text-slate-400 text-xs font-semibold mb-1 uppercase">Limit (LKR)</label>
                            <input type="number" required min="10000" step="10000" value={newLimit} onChange={(e) => setNewLimit(e.target.value)} className="w-full bg-slate-900 border border-slate-700 text-white p-3 rounded-xl outline-none text-sm font-mono" />
                        </div>
                        <button type="submit" className="w-full bg-purple-600 hover:bg-purple-500 text-white py-3 rounded-xl font-bold transition cursor-pointer">
                            {isOfficer ? 'Issue Instant Card' : 'Submit Card Application'}
                        </button>
                    </form>
                </div>

                {/* CARDS TABLE */}
                <div className="lg:col-span-2 bg-slate-950 p-6 rounded-2xl border border-slate-800 shadow-xl space-y-4">
                    <h2 className="text-xl font-bold text-white flex items-center justify-between">
                        <span>{isOfficer ? 'Card Officer Management & Approvals' : 'My Active & Requested Cards'}</span>
                        <span className="text-xs text-slate-400">{displayedCards.length} Total</span>
                    </h2>
                    <div className="overflow-x-auto">
                        <table className="w-full text-left text-slate-300">
                            <thead className="bg-slate-900 text-slate-400 text-xs uppercase border-b border-slate-800">
                            <tr>
                                <th className="p-3">Card / Holder</th>
                                <th className="p-3">Card Number</th>
                                <th className="p-3">Status</th>
                                <th className="p-3">Pending Request & Reason</th>
                                <th className="p-3 text-right">Actions</th>
                            </tr>
                            </thead>
                            <tbody className="divide-y divide-slate-800 text-sm">
                            {displayedCards.map((card) => (
                                <tr key={card.id} className="hover:bg-slate-900/50">
                                    <td className="p-3">
                                        <p className="font-bold text-white">{card.cardType}</p>
                                        <button
                                            onClick={() => { setSelectedProfile(card); setShowFullCard(false); }}
                                            className="text-xs text-purple-400 hover:underline flex items-center gap-1 font-semibold cursor-pointer">
                                            <Eye size={12} /> {card.holder} (View Details)
                                        </button>
                                    </td>
                                    <td className="p-3 font-mono text-xs font-semibold text-slate-300">
                                        {maskCardNumber(card.fullCardNumber)}
                                    </td>
                                    <td className="p-3">
                      <span className={`px-2.5 py-1 rounded-full text-xs font-bold ${
                          card.status === 'Active' ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20' :
                              card.status === 'Blocked' ? 'bg-rose-500/10 text-rose-400 border border-rose-500/20' : 'bg-amber-500/10 text-amber-400'
                      }`}>
                        {card.status}
                      </span>
                                    </td>

                                    {/* DISPLAY REASON */}
                                    <td className="p-3">
                                        <p className="text-xs text-amber-300 font-bold">{card.pendingReq}</p>
                                        {card.blockReason ? (
                                            <div className="mt-1 inline-flex items-center gap-1 px-2 py-0.5 rounded-md bg-rose-500/10 border border-rose-500/20 text-rose-300 text-[11px] font-medium">
                                                <Info size={11} className="text-rose-400 shrink-0" />
                                                <span>{card.blockReason}</span>
                                            </div>
                                        ) : (
                                            <span className="text-[11px] text-slate-500 italic block mt-0.5">No special reason</span>
                                        )}
                                    </td>

                                    {/* ACTIONS */}
                                    <td className="p-3 text-right">
                                        {isOfficer ? (
                                            <div className="flex justify-end items-center gap-1.5">
                                                <button onClick={() => handleToggleBlock(card.id)} title={card.status === 'Blocked' ? 'Unblock Card' : 'Block Card'} className={`p-1.5 rounded-lg text-xs font-bold cursor-pointer ${card.status === 'Blocked' ? 'bg-emerald-500/20 text-emerald-400 hover:bg-emerald-500/30' : 'bg-amber-500/20 text-amber-400 hover:bg-amber-500/30'}`}>
                                                    {card.status === 'Blocked' ? <Unlock size={14} /> : <Lock size={14} />}
                                                </button>
                                            </div>
                                        ) : (
                                            <div className="flex justify-end items-center gap-2">
                                                {card.status === 'Active' && card.pendingReq === 'None' && (
                                                    <button
                                                        onClick={() => setBlockModalCard(card)}
                                                        className="px-2.5 py-1.5 bg-rose-500/10 text-rose-400 hover:bg-rose-500/20 rounded-lg text-xs font-bold flex items-center gap-1 cursor-pointer border border-rose-500/20">
                                                        <ShieldAlert size={13} /> Block Card
                                                    </button>
                                                )}
                                                {card.status === 'Blocked' && card.pendingReq === 'None' && (
                                                    <button
                                                        onClick={() => handleUnblockRequest(card.id)}
                                                        className="px-2.5 py-1.5 bg-emerald-500/10 text-emerald-400 hover:bg-emerald-500/20 rounded-lg text-xs font-bold flex items-center gap-1 cursor-pointer border border-emerald-500/20">
                                                        <Unlock size={13} /> Request Unblock
                                                    </button>
                                                )}
                                            </div>
                                        )}
                                    </td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>

            {/* BLOCK MODAL */}
            {blockModalCard && (
                <div className="fixed inset-0 bg-black/80 backdrop-blur-sm flex items-center justify-center p-4 z-50">
                    <div className="bg-slate-900 border border-slate-700 p-6 rounded-3xl max-w-md w-full space-y-4 shadow-2xl">
                        <h3 className="text-lg font-bold text-white flex items-center gap-2">
                            <AlertTriangle className="text-rose-400" /> Request Card Block
                        </h3>
                        <p className="text-xs text-slate-400">
                            Please specify the reason for blocking your <strong className="text-white">{blockModalCard.cardType}</strong>.
                        </p>

                        <div className="space-y-3">
                            <div>
                                <label className="block text-slate-400 text-xs font-semibold mb-1 uppercase">Reason for Blocking</label>
                                <select
                                    value={selectedReason}
                                    onChange={(e) => setSelectedReason(e.target.value)}
                                    className="w-full bg-slate-950 border border-slate-800 text-white rounded-xl p-3 outline-none text-xs">
                                    <option value="Lost or Stolen Card">Lost or Stolen Card</option>
                                    <option value="Suspected Fraudulent Activity">Suspected Fraudulent Activity</option>
                                    <option value="Temporary Freeze (Personal Preference)">Temporary Freeze (Personal Preference)</option>
                                    <option value="Card Damaged">Card Damaged</option>
                                    <option value="Other">Other Reason</option>
                                </select>
                            </div>

                            {selectedReason === 'Other' && (
                                <div>
                                    <label className="block text-slate-400 text-xs font-semibold mb-1 uppercase">Specify Reason</label>
                                    <textarea
                                        rows="2"
                                        value={customReason}
                                        onChange={(e) => setCustomReason(e.target.value)}
                                        placeholder="Enter details..."
                                        className="w-full bg-slate-950 border border-slate-800 text-white p-3 rounded-xl outline-none text-xs"
                                    />
                                </div>
                            )}
                        </div>

                        <div className="flex gap-3 pt-2">
                            <button onClick={() => setBlockModalCard(null)} className="flex-1 bg-slate-800 text-slate-300 py-2.5 rounded-xl font-bold text-xs cursor-pointer">
                                Cancel
                            </button>
                            <button onClick={submitBlockRequest} className="flex-1 bg-rose-600 hover:bg-rose-500 text-white py-2.5 rounded-xl font-bold text-xs cursor-pointer">
                                Submit Block Request
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* VIEW DETAILS MODAL */}
            {selectedProfile && (
                <div className="fixed inset-0 bg-black/80 backdrop-blur-sm flex items-center justify-center p-4 z-50">
                    <div className="bg-slate-900 border border-slate-700 p-6 rounded-3xl max-w-md w-full space-y-4 shadow-2xl">
                        <h3 className="text-xl font-bold text-white flex items-center gap-2">
                            <CreditCard className="text-purple-400" /> Card Details
                        </h3>

                        <div className="bg-slate-950 p-4 rounded-xl text-xs text-slate-300 border border-slate-800 space-y-3">
                            <p className="flex justify-between"><span>Card Holder:</span> <strong className="text-white">{selectedProfile.holder}</strong></p>
                            <p className="flex justify-between"><span>Card Type:</span> <strong className="text-purple-400">{selectedProfile.cardType}</strong></p>

                            <div className="bg-slate-900 p-3 rounded-lg border border-slate-800 space-y-1">
                                <div className="flex justify-between items-center">
                                    <span className="text-slate-400 uppercase text-[10px] font-bold">Card Number</span>
                                    <button
                                        onClick={() => setShowFullCard(!showFullCard)}
                                        className="text-cyan-400 hover:text-cyan-300 text-xs font-semibold flex items-center gap-1 cursor-pointer">
                                        {showFullCard ? <EyeOff size={13} /> : <Eye size={13} />}
                                        {showFullCard ? 'Hide Details' : 'Show Full Number'}
                                    </button>
                                </div>
                                <p className="text-cyan-300 font-mono font-bold text-sm tracking-widest">
                                    {showFullCard ? selectedProfile.fullCardNumber : maskCardNumber(selectedProfile.fullCardNumber)}
                                </p>
                            </div>

                            <p className="flex justify-between pt-1"><span>Credit / Limit:</span> <strong className="text-emerald-400 font-mono">LKR {selectedProfile.limitAmount || selectedProfile.limit || '100,000'}</strong></p>
                            <p className="flex justify-between"><span>Status:</span> <strong className="text-amber-400">{selectedProfile.status}</strong></p>
                            {selectedProfile.blockReason && (
                                <div className="bg-rose-500/10 border border-rose-500/20 p-2.5 rounded-xl mt-2">
                                    <span className="text-[10px] font-bold uppercase text-rose-400 block mb-0.5">Reported Reason</span>
                                    <span className="text-rose-200 font-medium">{selectedProfile.blockReason}</span>
                                </div>
                            )}
                        </div>

                        <button onClick={() => setSelectedProfile(null)} className="w-full bg-slate-800 hover:bg-slate-700 text-white py-2.5 rounded-xl font-bold cursor-pointer">
                            Close
                        </button>
                    </div>
                </div>
            )}
        </div>
    );
}