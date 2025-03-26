// pages/MyPage.js
import React from "react";

const MyPage = () => {
    return (
        <div className="space-y-6">
            <h1 className="text-2xl font-bold">마이페이지</h1>

            <div className="flex items-center gap-6">
                <div className="flex flex-col items-center">
                    <div className="w-24 h-24 rounded-full bg-gray-300" />
                    <p className="mt-2 font-semibold">정찬우</p>
                    <p className="text-sm text-gray-500">chanwoo000</p>
                </div>

                <div className="flex-1 bg-gray-100 p-4 rounded">
                    <h2 className="text-lg font-semibold mb-2">수입 / 지출</h2>
                    <p>수입 : $3,000</p>
                    <p>지출 : $4,000</p>
                    <div className="mt-4 bg-gray-300 h-40 flex items-center justify-center rounded">그래프</div>
                </div>
            </div>

            <div className="grid grid-cols-2 gap-6">
                <div className="bg-gray-100 p-4 rounded">
                    <h3 className="font-semibold mb-2">최근 결제 내역</h3>
                    <ul className="list-disc list-inside text-sm">
                        <li>정찬우 목소리</li>
                        <li>이정욱 목소리</li>
                        <li>찬우 목소리</li>
                        <li>김종민 목소리</li>
                    </ul>
                </div>
                <div className="bg-gray-100 p-4 rounded">
                    <h3 className="font-semibold mb-2">최근 결제 내역</h3>
                    <ul className="list-disc list-inside text-sm">
                        <li>정찬우 목소리</li>
                        <li>이정욱 목소리</li>
                        <li>찬우 목소리</li>
                        <li>김종민 목소리</li>
                    </ul>
                </div>
            </div>
        </div>
    );
};

export default MyPage;