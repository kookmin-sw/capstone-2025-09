import React from 'react';

const SelectBox = ({ label, value, onChange, options, placeholder }) => {
  return (
    <div>
      <label className="block text-sm font-medium mb-1">{label}</label>
      <div className="relative w-full">
        <select
          value={value ?? ''}
          onChange={onChange}
          className="appearance-none w-full h-[40px] px-4 pr-10 py-2 rounded-md border border-[#D9D9D9] bg-white text-gray-800 font-medium focus:outline-none"
        >
          <option value="" disabled hidden>
            {placeholder}
          </option>
          {options.map(({ label, value }, i) => (
            <option key={i} value={value} className="text-black">
              {label}
            </option>
          ))}
        </select>

        {/* 아래 화살표 아이콘 */}
        <div className="pointer-events-none absolute inset-y-0 right-3 flex items-center">
          <svg
            className="w-4 h-4 text-gray-700"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
            xmlns="http://www.w3.org/2000/svg"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M19 9l-7 7-7-7"
            />
          </svg>
        </div>
      </div>
    </div>
  );
};

export default SelectBox;
