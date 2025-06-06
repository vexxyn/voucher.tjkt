<?php
require_once 'vendor/autoload.php';
use RouterOS\Client;
use RouterOS\Query;

class MikroTikHelper {
    private static $client = null;
    
    public static function getClient() {
        if (self::$client === null) {
            try {
                self::$client = new Client([
                    'host' => 'your.mikrotik.ip',
                    'user' => 'hotspot_user',
                    'pass' => 'hotspot_password',
                    'port' => 8728,
                    'timeout' => 5,
                    'attempts' => 3,
                    'delay' => 1
                ]);
            } catch (Exception $e) {
                error_log("MikroTik connection error: " . $e->getMessage());
                throw new Exception("Could not connect to MikroTik router");
            }
        }
        return self::$client;
    }
    
    public static function authenticateUser($username, $password) {
        try {
            $client = self::getClient();
            
            // Check user exists
            $query = new Query('/ip/hotspot/user/print');
            $query->where('name', $username);
            $users = $client->query($query)->read();
            
            if (empty($users)) {
                return ['success' => false, 'message' => 'User not found'];
            }
            
            $user = $users[0];
            
            // Verify password (this depends on your MikroTik config)
            if ($user['password'] !== $password) {
                return ['success' => false, 'message' => 'Invalid password'];
            }
            
            // Check if user is disabled
            if (isset($user['disabled']) && $user['disabled'] === 'true') {
                return ['success' => false, 'message' => 'Account disabled'];
            }
            
            // Check if already logged in
            $query = new Query('/ip/hotspot/active/print');
            $query->where('user', $username);
            $active = $client->query($query)->read();
            
            if (!empty($active)) {
                return ['success' => false, 'message' => 'User already logged in'];
            }
            
            // Perform login
            $query = new Query('/ip/hotspot/active/login');
            $query->equal('user', $username);
            $query->equal('password', $password);
            $response = $client->query($query)->read();
            
            return [
                'success' => true,
                'message' => 'Login successful',
                'profile' => $user['profile'] ?? null,
                'limit' => $user['limit-uptime'] ?? null
            ];
            
        } catch (Exception $e) {
            error_log("Authentication error: " . $e->getMessage());
            return ['success' => false, 'message' => 'Authentication error'];
        }
    }
    
    public static function validateVoucher($code) {
        try {
            $client = self::getClient();
            
            // Check voucher exists
            $query = new Query('/ip/hotspot/user/print');
            $query->where('name', $code);
            $query->where('comment', 'voucher');
            $vouchers = $client->query($query)->read();
            
            if (empty($vouchers)) {
                return ['success' => false, 'message' => 'Invalid voucher code'];
            }
            
            $voucher = $vouchers[0];
            
            // Check if voucher is disabled
            if (isset($voucher['disabled']) && $voucher['disabled'] === 'true') {
                return ['success' => false, 'message' => 'Voucher has been disabled'];
            }
            
            // Check if voucher is already used
            $query = new Query('/ip/hotspot/active/print');
            $query->where('user', $code);
            $active = $client->query($query)->read();
            
            if (!empty($active)) {
                return ['success' => false, 'message' => 'Voucher is already in use'];
            }
            
            // Get voucher duration
            $duration = isset($voucher['limit-uptime']) ? 
                self::uptimeToSeconds($voucher['limit-uptime']) : 3600;
            
            // Perform login with voucher
            $query = new Query('/ip/hotspot/active/login');
            $query->equal('user', $code);
            $response = $client->query($query)->read();
            
            return [
                'success' => true,
                'message' => 'Voucher accepted',
                'duration' => $duration,
                'profile' => $voucher['profile'] ?? null
            ];
            
        } catch (Exception $e) {
            error_log("Voucher validation error: " . $e->getMessage());
            return ['success' => false, 'message' => 'Voucher validation error'];
        }
    }
    
    public static function getNetworkStats() {
        try {
            $client = self::getClient();
            
            // Get interface statistics (simplified example)
            $query = new Query('/interface/monitor-traffic');
            $query->equal('interface', 'ether1');
            $query->equal('once');
            $traffic = $client->query($query)->read();
            
            // Calculate speed in Mbps
            $download = isset($traffic[0]['rx-bits-per-second']) ? 
                round($traffic[0]['rx-bits-per-second'] / 1000000, 1) : 0;
            $upload = isset($traffic[0]['tx-bits-per-second']) ? 
                round($traffic[0]['tx-bits-per-second'] / 1000000, 1) : 0;
            
            // Calculate usage in MB
            $usage = isset($traffic[0]['rx-byte'], $traffic[0]['tx-byte']) ? 
                round(($traffic[0]['rx-byte'] + $traffic[0]['tx-byte']) / (1024 * 1024), 1) : 0;
            
            return [
                'download' => $download,
                'upload' => $upload,
                'usage' => $usage,
                'unit' => 'Mbps'
            ];
            
        } catch (Exception $e) {
            error_log("Network stats error: " . $e->getMessage());
            return [
                'download' => 0,
                'upload' => 0,
                'usage' => 0,
                'unit' => 'Mbps'
            ];
        }
    }
    
    public static function getVoucherInfo($code) {
        try {
            $client = self::getClient();
            
            $query = new Query('/ip/hotspot/user/print');
            $query->where('name', $code);
            $query->where('comment', 'voucher');
            $vouchers = $client->query($query)->read();
            
            if (empty($vouchers)) {
                return null;
            }
            
            $voucher = $vouchers[0];
            
            return [
                'code' => $voucher['name'] ?? $code,
                'duration' => $voucher['limit-uptime'] ?? '1h',
                'created' => $voucher['creation-date'] ?? null,
                'expires' => $voucher['expiration-date'] ?? null,
                'used' => $voucher['last-logged-out'] ?? null,
                'profile' => $voucher['profile'] ?? null
            ];
            
        } catch (Exception $e) {
            error_log("Voucher info error: " . $e->getMessage());
            return null;
        }
    }
    
    private static function uptimeToSeconds($uptime) {
        $uptime = strtolower($uptime);
        $seconds = 0;
        
        $parts = [];
        preg_match_all('/(\d+)([dhmsw])/', $uptime, $matches);
        
        for ($i = 0; $i < count($matches[0]); $i++) {
            $value = (int)$matches[1][$i];
            $unit = $matches[2][$i];
            
            switch ($unit) {
                case 'd': $seconds += $value * 86400; break;
                case 'h': $seconds += $value * 3600; break;
                case 'm': $seconds += $value * 60; break;
                case 's': $seconds += $value; break;
                case 'w': $seconds += $value * 604800; break;
            }
        }
        
        return $seconds;
    }
}

// Wrapper functions for easier use
function getMikroTikNetworkStats() {
    return MikroTikHelper::getNetworkStats();
}

function authenticateUser($username, $password) {
    return MikroTikHelper::authenticateUser($username, $password);
}

function validateVoucher($code) {
    return MikroTikHelper::validateVoucher($code);
}

function getMikroTikVoucherInfo($code) {
    return MikroTikHelper::getVoucherInfo($code);
}
?>