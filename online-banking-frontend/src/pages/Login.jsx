import { useState } from 'react';
import { Landmark, Shield, User, Lock, ChevronRight, UserPlus, LogIn, Mail, Phone, CreditCard } from 'lucide-react';

export default function Login({ onLogin }) {
    const [isRegister, setIsRegister] = useState(false);

    // Login / Register Form States
    const [username, setUsername] = useState('Customer_User');
    const [password, setPassword] = useState('1234');
    const [role, setRole] = useState('CUSTOMER');

    // Web Profile Registration Fields (No Bank Account Info Here)
    const [fullName, setFullName] = useState('');
    const [nic, setNic] = useState('');
    const [email, setEmail] = useState('');
    const [phone, setPhone] = useState('');

    // Role එක වෙනස් කරනවිට automatic අදාළ Officer ගේ නම වැටීමට
    const handleRoleChange = (selectedRole) => {
        setRole(selectedRole);
        switch (selectedRole) {
            case 'LOAN_OFFICER':
                setUsername('Loan_Officer_Pathum');
                break;
            case 'CARD_OFFICER':
                setUsername('Card_Officer_Mathuziyanth');
                break;
            case 'INVESTMENT_OFFICER':
                setUsername('Investment_Officer_Gunarathna');
                break;
            case 'SUPPORT_OFFICER':
                setUsername('Support_Officer_Pathum');
                break;
            case 'ACCOUNT_OFFICER':
                setUsername('Account_Officer_Staff');
                break;
            case 'ADMIN':
                setUsername('Admin_Dassanayaka');
                break;
            default:
                setUsername('Customer_User');
                break;
        }
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        if (isRegister) {
            // Registering only the Web Profile
            alert(`Web Profile Created Successfully for ${fullName}!\n\nPlease log in and go to "Service Requests" to officially open a Bank Account.`);
            onLogin({ username: fullName, role: 'CUSTOMER' });
        } else {
            onLogin({ username, role });
        }
    };

    return (
        <div className="min-h-screen bg-slate-950 flex items-center justify-center p-4">
            <div className="w-full max-w-lg bg-slate-900 border border-slate-800 p-8 rounded-3xl shadow-2xl space-y-6 my-8">

                {/* Header */}
                <div className="text-center space-y-2">
                    <div className="inline-flex items-center justify-center p-4 bg-blue-500/10 text-blue-400 rounded-2xl mb-2">
                        <Landmark size={40} />
                    </div>
                    <h1 className="text-2xl font-black text-white tracking-wide">APEX BANKING PORTAL</h1>
                    <p className="text-slate-400 text-xs">
                        {isRegister ? 'Create an Online Banking Profile' : 'Select your System Role or Department to Log In'}
                    </p>
                </div>

                {/* Login / Register Switcher */}
                <div className="flex bg-slate-950 p-1 rounded-xl border border-slate-800 text-xs font-bold">
                    <button
                        type="button"
                        onClick={() => { setIsRegister(false); handleRoleChange('CUSTOMER'); }}
                        className={`flex-1 py-2 rounded-lg transition-all flex items-center justify-center gap-1.5 cursor-pointer ${!isRegister ? 'bg-blue-600 text-white shadow-md' : 'text-slate-400 hover:text-white'}`}>
                        <LogIn size={14} /> System Login
                    </button>
                    <button
                        type="button"
                        onClick={() => { setIsRegister(true); setRole('CUSTOMER'); setUsername(''); setPassword(''); }}
                        className={`flex-1 py-2 rounded-lg transition-all flex items-center justify-center gap-1.5 cursor-pointer ${isRegister ? 'bg-blue-600 text-white shadow-md' : 'text-slate-400 hover:text-white'}`}>
                        <UserPlus size={14} /> Register Profile
                    </button>
                </div>

                <form onSubmit={handleSubmit} className="space-y-4">

                    {/* REGISTER FORM FIELDS */}
                    {isRegister ? (
                        <>
                            <div className="bg-blue-500/10 border border-blue-500/20 p-3 rounded-xl mb-4">
                                <p className="text-xs text-blue-300 text-center">
                                    Create your web profile first. You can request to open a bank account from the dashboard after logging in.
                                </p>
                            </div>

                            <div>
                                <label className="block text-slate-400 text-xs font-semibold mb-1 uppercase">Full Name</label>
                                <div className="relative">
                                    <User className="absolute left-3 top-3.5 text-slate-500" size={18} />
                                    <input type="text" required placeholder="e.g. Kasun Perera" value={fullName} onChange={(e) => setFullName(e.target.value)} className="w-full bg-slate-950 border border-slate-800 text-white pl-10 pr-4 py-3 rounded-xl outline-none focus:border-blue-500 text-sm" />
                                </div>
                            </div>

                            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                <div>
                                    <label className="block text-slate-400 text-xs font-semibold mb-1 uppercase">NIC / Passport</label>
                                    <div className="relative">
                                        <CreditCard className="absolute left-3 top-3.5 text-slate-500" size={18} />
                                        <input type="text" required placeholder="199512345V" value={nic} onChange={(e) => setNic(e.target.value)} className="w-full bg-slate-950 border border-slate-800 text-white pl-10 pr-4 py-3 rounded-xl outline-none focus:border-blue-500 text-sm" />
                                    </div>
                                </div>
                                <div>
                                    <label className="block text-slate-400 text-xs font-semibold mb-1 uppercase">Phone Number</label>
                                    <div className="relative">
                                        <Phone className="absolute left-3 top-3.5 text-slate-500" size={18} />
                                        <input type="text" required placeholder="0771234567" value={phone} onChange={(e) => setPhone(e.target.value)} className="w-full bg-slate-950 border border-slate-800 text-white pl-10 pr-4 py-3 rounded-xl outline-none focus:border-blue-500 text-sm" />
                                    </div>
                                </div>
                            </div>

                            <div>
                                <label className="block text-slate-400 text-xs font-semibold mb-1 uppercase">Email Address</label>
                                <div className="relative">
                                    <Mail className="absolute left-3 top-3.5 text-slate-500" size={18} />
                                    <input type="email" required placeholder="kasun@gmail.com" value={email} onChange={(e) => setEmail(e.target.value)} className="w-full bg-slate-950 border border-slate-800 text-white pl-10 pr-4 py-3 rounded-xl outline-none focus:border-blue-500 text-sm" />
                                </div>
                            </div>

                            <div>
                                <label className="block text-slate-400 text-xs font-semibold mb-1 uppercase">Create Password</label>
                                <div className="relative">
                                    <Lock className="absolute left-3 top-3.5 text-slate-500" size={18} />
                                    <input type="password" required placeholder="Enter a secure password" value={password} onChange={(e) => setPassword(e.target.value)} className="w-full bg-slate-950 border border-slate-800 text-white pl-10 pr-4 py-3 rounded-xl outline-none focus:border-blue-500 text-sm" />
                                </div>
                            </div>
                        </>
                    ) : (
                        /* LOGIN FORM FIELDS */
                        <>
                            <div>
                                <label className="block text-slate-400 text-xs font-semibold mb-1 uppercase">Username</label>
                                <div className="relative">
                                    <User className="absolute left-3 top-3.5 text-slate-500" size={18} />
                                    <input type="text" required placeholder="Username" value={username} onChange={(e) => setUsername(e.target.value)} className="w-full bg-slate-950 border border-slate-800 text-white pl-10 pr-4 py-3 rounded-xl outline-none focus:border-blue-500 text-sm" />
                                </div>
                            </div>

                            <div>
                                <label className="block text-slate-400 text-xs font-semibold mb-1 uppercase">Password</label>
                                <div className="relative">
                                    <Lock className="absolute left-3 top-3.5 text-slate-500" size={18} />
                                    <input type="password" required value={password} onChange={(e) => setPassword(e.target.value)} className="w-full bg-slate-950 border border-slate-800 text-white pl-10 pr-4 py-3 rounded-xl outline-none focus:border-blue-500 text-sm" />
                                </div>
                            </div>

                            <div>
                                <label className="block text-slate-400 text-xs font-semibold mb-1 uppercase">Department / System Role</label>
                                <div className="relative">
                                    <Shield className="absolute left-3 top-3.5 text-slate-500" size={18} />
                                    <select value={role} onChange={(e) => handleRoleChange(e.target.value)} className="w-full bg-slate-950 border border-slate-800 text-white pl-10 pr-4 py-3 rounded-xl outline-none focus:border-blue-500 text-sm cursor-pointer">
                                        <option value="CUSTOMER">Customer (User Dashboard)</option>
                                        <option value="LOAN_OFFICER">Loan Officer (Loan Dept - Pathum)</option>
                                        <option value="CARD_OFFICER">Card Officer (Card Dept - Mathuziyanth)</option>
                                        <option value="INVESTMENT_OFFICER">Investment Officer (Investments - Gunarathna)</option>
                                        <option value="SUPPORT_OFFICER">Customer Support Officer (Tickets - Pathum M.G.)</option>
                                        <option value="ACCOUNT_OFFICER">Account Service Officer (Requests)</option>
                                        <option value="ADMIN">System Administrator (Full System - Dassanayaka)</option>
                                    </select>
                                </div>
                            </div>
                        </>
                    )}

                    <button type="submit" className="w-full bg-blue-600 hover:bg-blue-500 text-white py-3.5 rounded-xl font-bold transition-all shadow-lg hover:shadow-blue-500/25 flex items-center justify-center gap-2 cursor-pointer mt-2">
                        <span>{isRegister ? 'Register Profile' : 'Log In to Portal'}</span>
                        <ChevronRight size={18} />
                    </button>
                </form>
            </div>
        </div>
    );
}