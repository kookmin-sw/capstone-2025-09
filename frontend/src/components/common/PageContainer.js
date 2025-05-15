import React from 'react';

const PageContainer = ({ children, className = '' }) => {
  return (
    <div
      className={`w-full bg-white bg-opacity-50 p-8 rounded-3xl ${className}`}
    >
      {children}
    </div>
  );
};

export default PageContainer;