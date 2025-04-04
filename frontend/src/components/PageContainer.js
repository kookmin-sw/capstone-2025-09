import React from 'react';

const PageContainer = ({ children, className = '' }) => {
  return (
    <div
      className={`w-full max-w-7xl mx-auto my-5 bg-white bg-opacity-50 p-8 rounded-3xl shadow-md ${className}`}
    >
      {children}
    </div>
  );
};

export default PageContainer;
