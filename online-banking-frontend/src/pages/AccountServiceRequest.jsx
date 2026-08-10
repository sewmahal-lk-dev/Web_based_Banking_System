import { useState } from 'react';
import { UserCheck, Check, X, Eye, FileText, Download, ExternalLink } from 'lucide-react';

export default function AccountServiceRequest({ role = 'ACCOUNT_OFFICER' }) {
    const isOfficer = role === 'ACCOUNT_OFFICER' || role === 'ADMIN';

    const [requests, setRequests] = useState([
        {
            id: 'REQ-301',
            applicant: 'Pathum',
            type: 'Open Savings Account',
            details: 'Initial Deposit: LKR 5,000',
            accountNo: '-',
            status: 'Pending Approval',
            hasSlip: true,
            slipUrl: 'https://images.unsplash.com/photo-1554224155-8d04cb21cd6c?auto=format&fit=crop&q=80&w=600', // Sample Payment Slip Image
            depositAmount: '5,000.00',
            bankRef: 'SLIP-992014'
        },
        {
            id: 'REQ-302',
            applicant: 'Kasun Perera',
            type: 'Address & Profile Update',
            details: 'New Addr: No.45, Kandy Rd, Colombo',
            accountNo: '8001-4455-8812',
            status: 'Approved',
            hasSlip: false
        }
    ]);

    const [selectedSlip, setSelectedSlip] = useState(null);
    const [selectedApplicant, setSelectedApplicant] = useState(null);

    const handleApprove = (id) => {
        // Approve වෙනකොට Auto Account Number එකක් assign වෙනවා
        const generatedAccNo = `8001-${Math.floor(1000 + Math.random() * 9000)}-${Math.floor(1000 + Math.random() * 9000)}`;
        setRequests(requests.map(r => r.id === id ? { ...r, status: 'Approved', accountNo: generatedAccNo } : r));
        alert('Request Approved Successfully!');
    };

    const handleReject = (id) => {
        setRequests(requests.map(r => r.id === id ? { ...r, status: 'Rejected' } : r));
    };

    return (
        <div className="space-y-8">
            <div>
                <h1 className="text-3xl font-extrabold text-white flex items-center gap-3">
                    <UserCheck className="text-emerald-400" size={32} />
                    Account Service Requests
                </h1>
                <p className="text-slate-400 text-sm mt-1">
                    Logged in Role: <span className="text-cyan-400 font-semibold">{role}</span>
                </p>
            </div>

            <div className="bg-slate-950 p-6 rounded-2xl border border-slate-800 shadow-xl space-y-4">
                <div className="flex justify-between items-center">
                    <h2 className="text-xl font-bold text-white">Account Officer Queue</h2>
                    <span className="text-xs text-slate-400 font-semibold">{requests.length} Requests</span>
                </div>

                <div className="overflow-x-auto">
                    <table className="w-full text-left text-slate-300">
                        <thead className="bg-slate-900 text-slate-400 text-xs uppercase border-b border-slate-800">
                        <tr>
                            <th className="p-3">Applicant / Request</th>
                            <th className="p-3">Request Details</th>
                            <th className="p-3">Account No</th>
                            <th className="p-3">Status</th>
                            <th className="p-3 text-right">Actions</th>
                        </tr>
                        </thead>
                        <tbody className="divide-y divide-slate-800 text-sm">
                        {requests.map((req) => (
                            <tr key={req.id} className="hover:bg-slate-900/50">
                                <td className="p-3">
                                    <p className="font-bold text-white">{req.type}</p>
                                    <button
                                        onClick={() => setSelectedApplicant(req)}
                                        className="text-xs text-purple-400 hover:underline flex items-center gap-1 font-semibold cursor-pointer">
                                        <Eye size={12} /> {req.applicant}
                                    </button>
                                </td>

                                <td className="p-3">
                                    <p className="text-xs text-slate-300">{req.details}</p>
                                    {req.hasSlip && (
                                        <button
                                            onClick={() => setSelectedSlip(req)}
                                            className="mt-1 inline-flex items-center gap-1 px-2 py-0.5 rounded bg-blue-500/20 text-blue-400 text-[11px] font-bold hover:bg-blue-500/30 transition cursor-pointer">
                                            <FileText size={12} /> View Attached Slip
                                        </button>
                                    )}
                                </td>

                                <td className="p-3 font-mono text-xs text-cyan-400 font-semibold">{req.accountNo}</td>

                                <td className="p-3">
                    <span className={`px-2.5 py-1 rounded-full text-xs font-bold ${
                        req.status === 'Approved' ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20' :
                            req.status === 'Rejected' ? 'bg-rose-500/10 text-rose-400 border border-rose-500/20' : 'bg-amber-500/10 text-amber-400'
                    }`}>
                      {req.status}
                    </span>
                                </td>

                                <td className="p-3 text-right">
                                    {isOfficer && req.status === 'Pending Approval' ? (
                                        <div className="flex justify-end items-center gap-2">
                                            <button onClick={() => handleApprove(req.id)} className="px-3 py-1.5 bg-emerald-500/20 text-emerald-400 hover:bg-emerald-500/30 rounded-lg text-xs font-bold flex items-center gap-1 cursor-pointer">
                                                <Check size={14} /> Approve
                                            </button>
                                            <button onClick={() => handleReject(req.id)} className="px-3 py-1.5 bg-rose-500/20 text-rose-400 hover:bg-rose-500/30 rounded-lg text-xs font-bold flex items-center gap-1 cursor-pointer">
                                                <X size={14} /> Reject
                                            </button>
                                        </div>
                                    ) : (
                                        <span className="text-xs text-slate-500 italic">No Action Needed</span>
                                    )}
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </div>
            </div>

            {/* SLIP PREVIEW MODAL */}
            {selectedSlip && (
                <div className="fixed inset-0 bg-black/80 backdrop-blur-sm flex items-center justify-center p-4 z-50">
                    <div className="bg-slate-900 border border-slate-700 p-6 rounded-3xl max-w-lg w-full space-y-4 shadow-2xl">
                        <div className="flex justify-between items-center border-b border-slate-800 pb-3">
                            <h3 className="text-lg font-bold text-white flex items-center gap-2">
                                <FileText className="text-blue-400" /> Attached Deposit Slip
                            </h3>
                            <span className="text-xs font-mono bg-blue-500/10 text-blue-400 px-2 py-1 rounded-md">{selectedSlip.bankRef}</span>
                        </div>

                        <div className="space-y-3">
                            <div className="flex justify-between text-xs text-slate-300 bg-slate-950 p-3 rounded-xl border border-slate-800">
                                <span>Applicant: <strong className="text-white">{selectedSlip.applicant}</strong></span>
                                <span>Amount: <strong className="text-emerald-400">LKR {selectedSlip.depositAmount}</strong></span>
                            </div>

                            {/* Slip Image View */}
                            <div className="bg-slate-950 p-2 rounded-2xl border border-slate-800 flex justify-center items-center max-h-80 overflow-hidden">
                                <img
                                    src={selectedSlip.slipUrl}
                                    alt="Bank Deposit Slip"
                                    className="rounded-xl object-contain w-full max-h-72 hover:scale-105 transition duration-300"
                                />
                            </div>
                        </div>

                        <div className="flex items-center gap-3 pt-2">
                            <a
                                href={selectedSlip.slipUrl}
                                target="_blank"
                                rel="noreferrer"
                                className="flex-1 bg-slate-800 hover:bg-slate-700 text-slate-200 py-2.5 rounded-xl font-bold text-xs flex items-center justify-center gap-2">
                                <ExternalLink size={14} /> Open Full Size
                            </a>
                            <button
                                onClick={() => setSelectedSlip(null)}
                                className="flex-1 bg-purple-600 hover:bg-purple-500 text-white py-2.5 rounded-xl font-bold text-xs cursor-pointer">
                                Close
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* APPLICANT PROFILE MODAL */}
            {selectedApplicant && (
                <div className="fixed inset-0 bg-black/80 backdrop-blur-sm flex items-center justify-center p-4 z-50">
                    <div className="bg-slate-900 border border-slate-700 p-6 rounded-3xl max-w-md w-full space-y-4 shadow-2xl">
                        <h3 className="text-xl font-bold text-white">Applicant Profile</h3>
                        <div className="bg-slate-950 p-4 rounded-xl text-xs text-slate-300 border border-slate-800 space-y-2">
                            <p className="flex justify-between"><span>Name:</span> <strong className="text-white">{selectedApplicant.applicant}</strong></p>
                            <p className="flex justify-between"><span>Request ID:</span> <strong className="text-cyan-400">{selectedApplicant.id}</strong></p>
                            <p className="flex justify-between"><span>Request Type:</span> <strong className="text-purple-400">{selectedApplicant.type}</strong></p>
                        </div>
                        <button onClick={() => setSelectedApplicant(null)} className="w-full bg-slate-800 text-white py-2.5 rounded-xl font-bold cursor-pointer">Close</button>
                    </div>
                </div>
            )}
        </div>
    );
}