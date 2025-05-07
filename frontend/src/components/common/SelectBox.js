import { Listbox } from '@headlessui/react';
import { ChevronDownIcon } from '@heroicons/react/20/solid';
import React from 'react';

const SelectBox = ({ label, value, onChange, options, placeholder }) => {
  const selectedOption = options.find((opt) => opt.value === value);

  return (
    <div>
      <label className="block text-sm font-medium mb-1">{label}</label>
      <Listbox
        value={value}
        onChange={(val) => onChange({ target: { value: val } })}
      >
        <div className="relative min-w-[200px]">
          <Listbox.Button className=" max-w-full w-full overflow-hidden whitespace-nowrap text-ellipsis px-4 pr-10 py-2 text-left bg-white border border-[#D9D9D9] rounded-md font-medium text-gray-800 focus:outline-none">
            <span>
              {selectedOption ? (
                selectedOption.label
              ) : (
                <span className="text-gray-400">{placeholder}</span>
              )}
            </span>
            <span className="pointer-events-none absolute inset-y-0 right-3 flex items-center">
              <ChevronDownIcon className="w-4 h-4 text-gray-700" />
            </span>
          </Listbox.Button>

          <Listbox.Options className="absolute z-10 max-h-60 w-full overflow-y-auto bg-white border border-[#D9D9D9] rounded-md">
            {options.map(({ label, value }) => (
              <Listbox.Option
                key={value}
                value={value}
                className={({ active }) =>
                  `cursor-pointer select-none px-4 py-2 ${
                    active ? 'bg-violet-400 text-white' : 'text-gray-900'
                  }`
                }
              >
                {label}
              </Listbox.Option>
            ))}
          </Listbox.Options>
        </div>
      </Listbox>
    </div>
  );
};

export default SelectBox;
