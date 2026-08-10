import { useState } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate, Link } from 'react-router-dom';
import { Landmark, FileText, CreditCard, TrendingUp, UserCheck, HelpCircle, Shield, LogOut, LayoutDashboard } from 'lucide-react';

import Login from './pages/Login';
import CustomerDashboard from './pages/CustomerDashboard';
import LoanManagement from './pages/LoanManagement';
import CardManagement from './pages/CardManagement';
import InvestmentManagement from './pages/InvestmentManagement';
import AccountServiceRequest from './pages/AccountServiceRequest';
import CustomerSupport from './pages/CustomerSupport';
import AdminManagement from './pages/AdminManagement';

export default function App() {
  const [user, setUser] = useState(null);

  if (!user) {
    return <Login onLogin={(userData) => setUser(userData)} />;
  }

  const role = user.role;

  // Helper function for smart default routing
  const getDefaultRoute = () => {
    switch (role) {
      case 'CUSTOMER': return '/dashboard';
      case 'LOAN_OFFICER': return '/loans';
      case 'CARD_OFFICER': return '/cards';
      case 'INVESTMENT_OFFICER': return '/investments';
      case 'SUPPORT_OFFICER': return '/support';
      case 'ACCOUNT_OFFICER': return '/accounts';
      case 'ADMIN': return '/admin';
      default: return '/dashboard';
    }
  };

  return (
      <Router>
        <div className="flex h-screen bg-slate-900 text-white">
          {/* Sidebar Navigation */}
          <div className="w-64 bg-slate-950 p-5 flex flex-col justify-between border-r border-slate-800 shadow-2xl">
            <div>
              <div className="flex items-center gap-3 text-2xl font-bold mb-8 text-blue-500 tracking-wider">
                <Landmark className="animate-pulse text-blue-400" size={32} />
                <span>APEX BANK</span>
              </div>

              <nav className="space-y-2">
                {/* Customer Access */}
                {role === 'CUSTOMER' && (
                    <Link to="/dashboard" className="flex items-center gap-3 px-4 py-3 rounded-xl hover:bg-slate-800 transition text-slate-300 hover:text-white font-medium">
                      <LayoutDashboard size={20} className="text-blue-400" /> Account Dashboard
                    </Link>
                )}

                {/* Account Service Officer & Customer Access */}
                {(role === 'CUSTOMER' || role === 'ACCOUNT_OFFICER' || role === 'ADMIN') && (
                    <Link to="/accounts" className="flex items-center gap-3 px-4 py-3 rounded-xl hover:bg-slate-800 transition text-slate-300 hover:text-white font-medium">
                      <UserCheck size={20} className="text-emerald-400" /> {role === 'ACCOUNT_OFFICER' ? 'Account Service Dept' : 'Service Requests'}
                    </Link>
                )}

                {/* Departmental Access based on Officer Login */}
                {(role === 'CUSTOMER' || role === 'LOAN_OFFICER' || role === 'ADMIN') && (
                    <Link to="/loans" className="flex items-center gap-3 px-4 py-3 rounded-xl hover:bg-slate-800 transition text-slate-300 hover:text-white font-medium">
                      <FileText size={20} className="text-cyan-400" /> {role === 'LOAN_OFFICER' ? 'Loan Officer Dept' : 'Loans'}
                    </Link>
                )}

                {(role === 'CUSTOMER' || role === 'CARD_OFFICER' || role === 'ADMIN') && (
                    <Link to="/cards" className="flex items-center gap-3 px-4 py-3 rounded-xl hover:bg-slate-800 transition text-slate-300 hover:text-white font-medium">
                      <CreditCard size={20} className="text-purple-400" /> {role === 'CARD_OFFICER' ? 'Card Officer Dept' : 'Cards'}
                    </Link>
                )}

                {(role === 'CUSTOMER' || role === 'INVESTMENT_OFFICER' || role === 'ADMIN') && (
                    <Link to="/investments" className="flex items-center gap-3 px-4 py-3 rounded-xl hover:bg-slate-800 transition text-slate-300 hover:text-white font-medium">
                      <TrendingUp size={20} className="text-amber-400" /> {role === 'INVESTMENT_OFFICER' ? 'Investment Dept' : 'Investments'}
                    </Link>
                )}

                {(role === 'CUSTOMER' || role === 'SUPPORT_OFFICER' || role === 'ADMIN') && (
                    <Link to="/support" className="flex items-center gap-3 px-4 py-3 rounded-xl hover:bg-slate-800 transition text-slate-300 hover:text-white font-medium">
                      <HelpCircle size={20} className="text-rose-400" /> {role === 'SUPPORT_OFFICER' ? 'Support Officer Dept' : 'Support Tickets'}
                    </Link>
                )}

                {/* Admin Access */}
                {role === 'ADMIN' && (
                    <Link to="/admin" className="flex items-center gap-3 px-4 py-3 rounded-xl hover:bg-slate-800 transition text-red-400 border border-red-500/30 font-bold mt-4">
                      <Shield size={20} /> Admin & Users Portal
                    </Link>
                )}
              </nav>
            </div>

            <button onClick={() => setUser(null)} className="flex items-center gap-3 px-4 py-3 bg-red-600/20 text-red-400 rounded-xl hover:bg-red-600 hover:text-white transition cursor-pointer font-semibold">
              <LogOut size={20} /> Logout ({user.username})
            </button>
          </div>

          {/* Dynamic Route Handlers */}
          <div className="flex-1 overflow-y-auto bg-slate-900 p-8">
            <Routes>
              <Route path="/dashboard" element={<CustomerDashboard user={user} />} />
              <Route path="/accounts" element={<AccountServiceRequest role={role} />} />
              <Route path="/loans" element={<LoanManagement role={role} />} />
              <Route path="/cards" element={<CardManagement role={role} />} />
              <Route path="/investments" element={<InvestmentManagement role={role} />} />
              <Route path="/support" element={<CustomerSupport role={role} />} />
              <Route path="/admin" element={<AdminManagement role={role} />} />
              <Route path="*" element={<Navigate to={getDefaultRoute()} />} />
            </Routes>
          </div>
        </div>
      </Router>
  );
}