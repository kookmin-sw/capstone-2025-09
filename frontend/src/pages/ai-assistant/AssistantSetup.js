import React, { useState } from 'react';
import GradientButton from '../../components/common/GradientButton';
import SelectBox from '../../components/common/SelectBox';
import useVoicepackUsage from '../../hooks/useVoicepackUsage';
import useAssistantSetup from '../../hooks/useAssistantSetup';

// 상수로 유지될 항목들
const WRITING_STYLES = ['존댓말', '반말', '밝은 톤', '차분한 톤'];
const CATEGORIES = ['BBC 뉴스', 'Google 뉴스', 'IT 뉴스', '경제', '스포츠'];

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

  const isValid =
    selectedVoiceId !== null &&
    selectedWritingStyle !== null &&
    Array.isArray(selectedCategories) &&
    selectedCategories.length > 0;

  return (
    <div className="space-y-6">
      <h1 className="text-xl font-semibold">AI 리포터</h1>
      <p className="text-sm text-slate-500">
        음성 뉴스 요약 리포트를 위한 기본 정보를 설정해주세요.
      </p>

      {/* 보이스팩 & 문체 선택 */}
      <div className="flex flex-col md:flex-row mb-4 gap-4">
        <div className="w-full md:w-1/4">
          <SelectBox
            label="보이스팩"
            value={selectedVoiceId}
            onChange={(val) => setSelectedVoiceId(Number(val))}
            options={voicepackOptions}
            placeholder={placeholderText}
          />
        </div>
        <div className="w-full md:w-1/4">
          <SelectBox
            label="문체"
            value={selectedWritingStyle}
            onChange={(val) => setSelectedWritingStyle(Number(val))}
            options={writingStyleOptions}
            placeholder="문체를 선택해주세요."
          />
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
            disabled={!isValid}
          >
            세팅하기
          </GradientButton>
        </div>
      </div>
      );
      };

      export default AssistantSetup;
