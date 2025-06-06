<?php
// Authentication helper functions

function verifyCredentials($username, $password) {
    // In a real implementation, this would verify against MikroTik or database
    return !empty($username) && !empty($password);
}

function verifyVoucher($code) {
    // In a real implementation, this would verify against MikroTik or database
    return !empty($code) && strlen($code) >= 8;
}

function logAuthAttempt($ip, $username, $success) {
    // Log authentication attempts for security monitoring
    $logEntry = sprintf(
        "[%s] %s: %s from %s\n",
        date('Y-m-d H:i:s'),
        $success ? 'SUCCESS' : 'FAILURE',
        $username,
        $ip
    );
    
    file_put_contents('auth.log', $logEntry, FILE_APPEND);
}

function rateLimitCheck($ip) {
    // Simple rate limiting implementation
    $maxAttempts = 5;
    $timeWindow = 300; // 5 minutes
    
    $log = file_exists('auth.log') ? file('auth.log') : [];
    $attempts = 0;
    
    foreach ($log as $line) {
        if (strpos($line, $ip) !== false && strpos($line, 'FAILURE') !== false) {
            $timestamp = strtotime(substr($line, 1, 19));
            if (time() - $timestamp < $timeWindow) {
                $attempts++;
            }
        }
    }
    
    return $attempts < $maxAttempts;
}
?>