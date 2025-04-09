import React, { useState } from 'react';
import GradientButton from '../../components/common/GradientButton';
import useVoicepackUsage from '../../hooks/useVoicepackUsage';

// 상수로 유지될 항목들
const WRITING_STYLES = ['존댓말', '반말', '밝은 톤', '차분한 톤'];
const CATEGORIES = [
  '오늘의 명언',
  '오늘의 날씨',
  '오늘의 뉴스',
  'IT 소식',
  '유머',
];

const AssistantSetup = ({ setIsConfigured }) => {
  // 유저가 소유한 보이스팩: API(/api/voicepack/usage-right) 연동 예정 (현재는 더미 데이터)
  const { voicepacks = [] } = useVoicepackUsage();

  const [selectedVoiceId, setSelectedVoiceId] = useState('');
  const [selectedWritingStyle, setSelectedWritingStyle] = useState('');
  const [selectedCategories, setSelectedCategories] = useState([]);

  const toggleCategory = (index) => {
    const alreadySelected = selectedCategories.includes(index);
    if (alreadySelected) {
      setSelectedCategories((prev) => prev.filter((i) => i !== index));
    } else {
      if (selectedCategories.length >= 3) return;
      setSelectedCategories((prev) => [...prev, index]);
    }
  };

  const handleSetting = () => {
    const writingStyleIndex = WRITING_STYLES.findIndex(
      (style) => style === selectedWritingStyle
    );

    const config = {
      voicepackId: selectedVoiceId,
      writingStyle: writingStyleIndex,
      categories: selectedCategories,
    };

    console.log('🧠 API 요청용 설정 데이터:', config);
    localStorage.setItem('ai-assistant-config', JSON.stringify(config));
    setIsConfigured(true);
  };

  return (
    <div className="space-y-6">
      <h1 className="text-xl font-semibold">AI 비서</h1>

      {/* 보이스팩 & 문체 선택 */}
      <div className="grid grid-cols-2 gap-6">
        <div>
          <label className="block text-sm font-medium mb-1">보이스팩</label>
          <select
            value={selectedVoiceId}
            onChange={(e) => setSelectedVoiceId(Number(e.target.value))}
            className="w-full border border-gray-300 rounded-md px-4 py-2 text-sm"
          >
            <option value="" disabled hidden>
              보이스팩을 선택해주세요.
            </option>
            {Array.isArray(voicepacks) &&
              voicepacks.map(({ voicepackId, voicepackName }) => (
                <option key={voicepackId} value={voicepackId}>
                  {voicepackName}
                </option>
              ))}
          </select>
        </div>
        <div>
          <label className="block text-sm font-medium mb-1">문체</label>
          <select
            value={selectedWritingStyle}
            onChange={(e) => setSelectedWritingStyle(e.target.value)}
            className="w-full border border-gray-300 rounded-md px-4 py-2 text-sm"
          >
            <option value="" disabled hidden selected>
              문체를 선택해주세요.
            </option>
            {WRITING_STYLES.map((name, i) => (
              <option key={i} value={name}>
                {name}
              </option>
            ))}
          </select>
        </div>
      </div>

      {/* 카테고리 */}
      <div>
        <p className="text-sm font-medium mb-2">카테고리 (최대 3개)</p>
        <div className="flex gap-4 flex-wrap">
          {CATEGORIES.map((name, i) => {
            const selected = selectedCategories.includes(i);
            return (
              <button
                key={i}
                onClick={() => toggleCategory(i)}
                className={`px-6 py-2 rounded-md font-medium transition ${
                  selected
                    ? 'bg-[#A88BFF] text-white'
                    : 'border border-gray-300 text-gray-700 hover:bg-gray-100'
                }`}
              >
                {name}
              </button>
            );
          })}
        </div>
      </div>

      {/* 세팅 버튼 */}
      <div className="flex justify-end">
        <GradientButton
          className="px-6 py-3"
          onClick={handleSetting}
          disabled={
            !selectedVoiceId ||
            !selectedWritingStyle ||
            selectedCategories.length === 0
          }
        >
          세팅하기
        </GradientButton>
      </div>
    </div>
  );
};

export default AssistantSetup;
