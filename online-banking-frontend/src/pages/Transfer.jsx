import React, { useState } from 'react';
import { Send } from 'lucide-react';

export default function Transfer() {
    const [fromAcc, setFromAcc] = useState('');
    const [toAcc, setToAcc] = useState('');
    const [amount, setAmount] = useState('');

    const handleTransfer = (e) => {
        e.preventDefault();
        alert(`Transferred Rs. ${amount} from ${fromAcc} to ${toAcc} successfully!`);
        setFromAcc('');
        setToAcc('');
        setAmount('');
    };

    return (
        <div className="p-6 max-w-2xl mx-auto">
            <div className="mb-6 text-center">
                <h1 className="text-2xl font-bold text-gray-800">Fund Transfer</h1>
                <p className="text-gray-500 text-sm">Send money instantly between bank accounts</p>
            </div>

            <div className="bg-white p-6 rounded-xl shadow-md border border-gray-100">
                <form onSubmit={handleTransfer} className="space-y-4">
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">From Account</label>
                        <input
                            type="text"
                            required
                            placeholder="e.g. ACC-1001"
                            value={fromAcc}
                            onChange={(e) => setFromAcc(e.target.value)}
                            className="w-full border border-gray-300 p-3 rounded-lg outline-none focus:border-blue-500"
                        />
                    </div>

                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">To Account</label>
                        <input
                            type="text"
                            required
                            placeholder="e.g. ACC-1002"
                            value={toAcc}
                            onChange={(e) => setToAcc(e.target.value)}
                            className="w-full border border-gray-300 p-3 rounded-lg outline-none focus:border-blue-500"
                        />
                    </div>

                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">Transfer Amount (LKR)</label>
                        <input
                            type="number"
                            required
                            placeholder="0.00"
                            value={amount}
                            onChange={(e) => setAmount(e.target.value)}
                            className="w-full border border-gray-300 p-3 rounded-lg outline-none focus:border-blue-500"
                        />
                    </div>

                    <button
                        type="submit"
                        className="w-full bg-blue-600 hover:bg-blue-700 text-white py-3 rounded-lg font-medium transition flex items-center justify-center gap-2 shadow-md cursor-pointer">
                        <Send size={18} /> Transfer Now
                    </button>
                </form>
            </div>
        </div>
    );
}