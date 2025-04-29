import React from 'react';
import PropTypes from 'prop-types';

const GradientButton = ({
  children,
  onClick,
  className = '',
  type = 'button',
}) => {
  return (
    <button
      type={type}
      onClick={onClick}
      className={`bg-gradient-to-r from-violet-400 to-indigo-500 text-white font-semibold rounded-lg shadow-sm hover:opacity-70 transition ${className}`}
    >
      {children}
    </button>
  );
};

GradientButton.propTypes = {
  children: PropTypes.node.isRequired,
  onClick: PropTypes.func,
  className: PropTypes.string,
  type: PropTypes.string,
};

export default GradientButton;
