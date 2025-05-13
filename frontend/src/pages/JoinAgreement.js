import React, {useState} from 'react';
import {useNavigate} from "react-router-dom";

const JoinAgreement = () => {
  const [agreed, setAgreed] = useState(false);
  const navigate = useNavigate();

  const handleAgreeAndNext = () => {
    // 동의 여부를 sessionStorage에 저장
    sessionStorage.setItem('agreedToTerms', 'true');
    navigate('/sign-up');
  };

  const handleCheckboxChange = (e) => {
    setAgreed(e.target.checked);
  };

  return (
    <div className="max-w-3xl mx-auto p-8">
      <div className="flex flex-col items-center mb-4">
        <h1 className="text-2xl font-bold mb-4">회원가입</h1>
        <p>회원가입 전, Covos 약관을 확인해주세요.</p>
      </div>
      <section className="text-sm space-y-4 max-h-[430px] overflow-y-auto border p-4 rounded-lg bg-slate-100">
        <p>
          본 약관은 「보이스팩 플랫폼」(이하 “Covos”)이 제공하는 모든 서비스에 적용됩니다.
          이용자는 회원가입 또는 서비스 이용 시 본 약관에 동의한 것으로 간주되며,
          약관·법령 위반으로 발생한 모든 법적 책임은 이용자에게 있습니다.
        </p>

        <h2 className="font-semibold mt-4">제1조 (권리 보장 및 의무)</h2>
        <p className="font-semibold">지식재산권</p>
        <ul className="list-disc ml-6">
          <li>이용자는 업로드·판매·배포하는 모든 보이스팩에 대해 정당한 권리를 보유해야 합니다.</li>
          <li>제3자의 저작권·음성권(퍼블리시티권)·초상권을 침해할 경우, 플랫폼은 사전 통보 없이 해당 콘텐츠를 제거하고 손해배상을 청구할 수 있습니다.</li>
        </ul>
        <p className="font-semibold">허가·고지 의무</p>
        <ul className="list-disc ml-6">
          <li>공인·성우 등 실제 인물의 음성을 합성·판매할 때는 사전 서면 허가를 받아야 합니다.</li>
          <li>방송·언론·정치·금융 분야 오디오에는 ‘AI 합성 음성’임을 명확히 고지해야 합니다.</li>
        </ul>

        <h2 className="font-semibold mt-4">제2조 (금지 행위)</h2>
        <ul className="list-disc ml-6">
          <li>불법·유해 목적(증오·차별·성적 착취·폭력·테러 조장 등)의 음성 합성·배포</li>
          <li>허위정보·딥페이크 제작 및 유포</li>
          <li>미성년자 음성의 무단 거래·유해 콘텐츠 사용</li>
          <li>자동화 스크립트·봇을 통한 대량 요청, 서비스 장애 유발</li>
          <li>결제·환불 과정에서의 사기, 로깅 조작 시도</li>
        </ul>
        <p>위 행위 적발 시 플랫폼은 콘텐츠 삭제·계정 정지·법적 조치를 즉시 취할 수 있습니다.</p>

        <h2 className="font-semibold mt-4">제3조 (개인정보 및 미성년자 보호)</h2>
        <ul className="list-disc ml-6">
          <li>음성·메타데이터에 타인 식별 정보(실명·연락처 등)를 포함해서는 안 됩니다.</li>
          <li>만 19세 미만 이용자는 법정대리인 동의 없이 보이스팩을 판매·거래할 수 없습니다.</li>
        </ul>

        <h2 className="font-semibold mt-4">제4조 (거래·결제)</h2>
        <ul className="list-disc ml-6">
          <li>크레딧 충전·환불·정산은 플랫폼 정책·관계 법령에 따릅니다.</li>
          <li>불법 결제 또는 사기 거래 발생 시 모든 손해는 해당 이용자가 배상하며, 플랫폼은 관련 로그·거래 내역을 수사기관에 제출할 수 있습니다.</li>
        </ul>

        <h2 className="font-semibold mt-4">제5조 (로그 제공 및 책임 전가)</h2>
        <p>
          이용자가 약관 또는 법령을 위반하여 분쟁·수사·소송이 발생할 경우,
          플랫폼은 IP, 접속 기록, 결제 내역, 음성 합성·다운로드 로그 등을 관계 기관에 제공할 수 있습니다.
          그로 인해 발생하는 민·형사상 책임 및 비용은 전적으로 해당 이용자에게 귀속됩니다.
        </p>

        <h2 className="font-semibold mt-4">제6조 (서비스 이용 제한·해지)</h2>
        <p>
          플랫폼은 필요 시 사전 통보 없이 콘텐츠 삭제, 판매 중지, 계정 정지, 회원 탈퇴 조치를 할 수 있습니다.
          이용자는 해지 이후에도 위반 행위로 인한 책임에서 면책되지 않습니다.
        </p>

        <h2 className="font-semibold mt-4">제7조 (약관 변경)</h2>
        <p>
          플랫폼은 서비스 개선·법령 개정에 따라 약관을 변경할 수 있으며,
          변경 7일 전 홈페이지에 공지합니다. 이용자가 변경 약관에 동의하지 않을 경우,
          회원 탈퇴로 거부 의사를 표시할 수 있습니다.
        </p>

        <h2 className="font-semibold mt-4">제8조 (준거법 및 관할)</h2>
        <p>
          본 약관은 대한민국 법을 준거법으로 하며,
          플랫폼과 이용자 간 분쟁은 서울중앙지방법원을 전속 관할로 합니다.
        </p>

        <div className="mt-6 p-4 bg-gray-100 rounded">
          <strong>▣ 동의 확인</strong>
          <p>
            가입 또는 서비스 이용 버튼을 클릭·탭하는 행위는 위 모든 조항에 동의함을 의미합니다.
          </p>
        </div>
      </section>

      <div>

      </div>
      <div className="mt-4 flex justify-end items-center">
        <input
          type="checkbox"
          id="agree"
          className="mr-2"
          checked={agreed}
          onChange={handleCheckboxChange}
        />
        <label htmlFor="agree" className="text-sm">
          위 약관에 동의합니다. (필수)
        </label>
      </div>
      <div className="flex justify-center  ">
        <button
          onClick={handleAgreeAndNext}
          disabled={!agreed}
          className="mt-4 px-20 py-2 bg-gradient-to-r from-violet-400 to-indigo-500 text-white font-semibold rounded-lg shadow-sm disabled:opacity-50"
        >
          다음
        </button>
      </div>
    </div>
  );
};

export default JoinAgreement;
