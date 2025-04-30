import React from 'react';

const Section = ({ title, icon, children, className = '' }) => (
  <div className={`bg-white rounded-xl p-4 ${className}`}>
    <h3 className="text-sm font-medium mb-2 flex items-center gap-2 text-gray-600">
      <span className="text-base">{icon}</span> {title}
    </h3>
    <div className="text-xs text-gray-700 leading-relaxed overflow-y-auto ">
      {children}
    </div>
  </div>
);

export default Section;
