import React, { useState } from 'react';
import GradientButton from '../../components/common/GradientButton';
import SelectBox from '../../components/common/SelectBox';
import useUserStore from '../../utils/userStore';
import useVoicepackUsage from '../../hooks/useVoicepackUsage';
import useAssistantSetup from '../../hooks/useAssistantSetup';

// 상수로 유지될 항목들
const WRITING_STYLES = ['존댓말', '반말', '밝은 톤', '차분한 톤'];
const CATEGORIES = ['BBC 뉴스', 'Google 뉴스', 'IT 뉴스'];

const AssistantSetup = ({ setIsConfigured }) => {
  const voicepacksRaw = useVoicepackUsage('available').voicepacks;
  const voicepacks = Array.isArray(voicepacksRaw) ? voicepacksRaw : [];

  const [selectedVoiceId, setSelectedVoiceId] = useState(null);
  const [selectedWritingStyle, setSelectedWritingStyle] = useState(null);
  const [selectedCategories, setSelectedCategories] = useState([]);

  const postSettings = useAssistantSetup();

  const writingStyleOptions = WRITING_STYLES.map((style, index) => ({
    label: style,
    value: index,
  }));

  const hasVoicepacks = Array.isArray(voicepacks) && voicepacks.length > 0;

  const voicepackOptions = hasVoicepacks
    ? voicepacks
        .filter((v) => v?.id && v?.name)
        .map(({ id, name }) => ({
          label: name,
          value: id,
        }))
    : [];

  const placeholderText = hasVoicepacks
    ? '보이스팩을 선택해주세요.'
    : '보이스팩이 없습니다.';

  const toggleCategory = (index) => {
    const alreadySelected = selectedCategories.includes(index);
    if (alreadySelected) {
      setSelectedCategories((prev) => prev.filter((i) => i !== index));
    } else {
      if (selectedCategories.length >= 3) return;
      setSelectedCategories((prev) => [...prev, index]);
    }
  };

  const handleSetting = async () => {
    const sortedCategories = [...selectedCategories].sort((a, b) => a - b);

    const config = {
      voicepackId: selectedVoiceId,
      writingStyle: selectedWritingStyle,
      categories: sortedCategories,
    };

    await postSettings(config);

    localStorage.setItem('ai-assistant-config', JSON.stringify(config));
    setIsConfigured(true);
  };

  return (
    <div className="space-y-6">
      <h1 className="text-xl font-semibold">AI 비서</h1>

      {/* 보이스팩 & 문체 선택 */}
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
        <SelectBox
          label="보이스팩"
          value={selectedVoiceId}
          onChange={(e) => setSelectedVoiceId(Number(e.target.value))}
          options={voicepackOptions}
          placeholder={placeholderText}
        />
        <SelectBox
          label="문체"
          value={selectedWritingStyle}
          onChange={(e) => setSelectedWritingStyle(Number(e.target.value))}
          options={writingStyleOptions}
          placeholder="문체를 선택해주세요."
        />
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
                    : 'border border-gray-300 bg-white text-gray-700 hover:bg-gray-100'
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
