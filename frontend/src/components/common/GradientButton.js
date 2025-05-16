import React from 'react';
import PropTypes from 'prop-types';

const GradientButton = ({
  children,
  onClick,
  className = '',
  type = 'button',
  disabled = false,
}) => {
  return (
    <button
      type={type}
      onClick={onClick}
      disabled={disabled}
      className={`bg-gradient-to-r from-violet-400 to-indigo-500 text-white font-semibold rounded-lg shadow-sm hover:opacity-70  disabled:opacity-50  transition ${className}`}
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
  disabled: PropTypes.bool,
};

export default GradientButton;
