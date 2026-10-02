-- ============================================================
-- QueueLess Seed Data
-- Run after all services have started (tables auto-created by JPA)
-- ============================================================

-- NOTE: Default users are auto-created by DataInitializer on user-service startup:
--   admin@gmail.com   / admin123
--   staff@gmail.com   / staff123
--   customer@gmail.com / customer123

USE queueless_business_db;

-- Businesses
INSERT INTO businesses (id, name, category, description, address, city, phone, average_service_time, open, created_at) VALUES
('a1b2c3d4-0001-0001-0001-000000000001', 'City Care Clinic',       'CLINIC',            'General healthcare and consultations',     '12 MG Road',          'Pune',    '020-11112222', 15, true,  NOW()),
('a1b2c3d4-0002-0002-0002-000000000002', 'Metro Diagnostics',      'DIAGNOSTIC_CENTER', 'Full-body checkups and lab tests',         '45 Baner Road',       'Pune',    '020-33334444', 20, true,  NOW()),
('a1b2c3d4-0003-0003-0003-000000000003', 'Style Studio',           'SALON',             'Premium hair and styling services',        '8 FC Road',           'Pune',    '020-55556666', 25, true,  NOW()),
('a1b2c3d4-0004-0004-0004-000000000004', 'QuickFix Service Center','SERVICE_CENTER',     'Mobile and electronics repair',            '22 Viman Nagar',      'Pune',    '020-77778888', 30, true,  NOW()),
('a1b2c3d4-0005-0005-0005-000000000005', 'Sunrise Clinic',         'CLINIC',            'Family medicine and pediatrics',           '5 Koregaon Park',     'Pune',    '020-99990000', 15, true,  NOW()),
('a1b2c3d4-0006-0006-0006-000000000006', 'The Barber Room',        'SALON',             'Classic cuts and grooming',                '3 Aundh Road',        'Mumbai',  '022-11112222', 20, false, NOW()),
('a1b2c3d4-0007-0007-0007-000000000007', 'Central Passport Office','GOVERNMENT_SERVICE','Passport application and renewal',         '1 Civil Lines',       'Mumbai',  '022-33334444', 45, true,  NOW()),
('a1b2c3d4-0008-0008-0008-000000000008', 'The Diner',              'RESTAURANT',        'Walk-in dining with virtual queue',        '77 Linking Road',     'Mumbai',  '022-55556666', 40, true,  NOW());

-- Services for City Care Clinic
INSERT INTO services (id, business_id, name, description, estimated_minutes, active) VALUES
('b1000001-0001-0001-0001-000000000001', 'a1b2c3d4-0001-0001-0001-000000000001', 'General Consultation', 'Walk-in consultation with a general physician', 15, true),
('b1000001-0001-0001-0001-000000000002', 'a1b2c3d4-0001-0001-0001-000000000001', 'Follow-up Visit',      'Follow-up for existing patients',              10, true);

-- Services for Metro Diagnostics
INSERT INTO services (id, business_id, name, description, estimated_minutes, active) VALUES
('b1000002-0002-0002-0002-000000000001', 'a1b2c3d4-0002-0002-0002-000000000002', 'Blood Test',           'Complete blood count and basic panel',         20, true),
('b1000002-0002-0002-0002-000000000002', 'a1b2c3d4-0002-0002-0002-000000000002', 'Full Body Checkup',    'Comprehensive health screening',               60, true);

-- Services for Style Studio
INSERT INTO services (id, business_id, name, description, estimated_minutes, active) VALUES
('b1000003-0003-0003-0003-000000000001', 'a1b2c3d4-0003-0003-0003-000000000003', 'Haircut',              'Wash, cut and blow-dry',                       25, true),
('b1000003-0003-0003-0003-000000000002', 'a1b2c3d4-0003-0003-0003-000000000003', 'Hair Spa',             'Deep conditioning treatment',                  45, true),
('b1000003-0003-0003-0003-000000000003', 'a1b2c3d4-0003-0003-0003-000000000003', 'Styling',              'Event or occasion styling',                    30, true);

-- Services for QuickFix
INSERT INTO services (id, business_id, name, description, estimated_minutes, active) VALUES
('b1000004-0004-0004-0004-000000000001', 'a1b2c3d4-0004-0004-0004-000000000004', 'Screen Replacement',   'Mobile screen repair',                         30, true),
('b1000004-0004-0004-0004-000000000002', 'a1b2c3d4-0004-0004-0004-000000000004', 'Battery Replacement',  'Battery swap for phones and laptops',          20, true);

USE queueless_queue_db;

-- Queues for City Care Clinic
INSERT INTO queues (id, business_id, queue_name, status, current_token_number, average_service_time, opened_at) VALUES
('c1000001-0001-0001-0001-000000000001', 'a1b2c3d4-0001-0001-0001-000000000001', 'General Consultation', 'OPEN', 21, 15, NOW());

-- Queues for Metro Diagnostics
INSERT INTO queues (id, business_id, queue_name, status, current_token_number, average_service_time, opened_at) VALUES
('c1000002-0002-0002-0002-000000000001', 'a1b2c3d4-0002-0002-0002-000000000002', 'Lab Tests', 'OPEN', 8, 20, NOW());

-- Queues for Style Studio
INSERT INTO queues (id, business_id, queue_name, status, current_token_number, average_service_time, opened_at) VALUES
('c1000003-0003-0003-0003-000000000001', 'a1b2c3d4-0003-0003-0003-000000000003', 'Hair Services', 'OPEN', 5, 25, NOW());

-- Queues for QuickFix
INSERT INTO queues (id, business_id, queue_name, status, current_token_number, average_service_time, opened_at) VALUES
('c1000004-0004-0004-0004-000000000001', 'a1b2c3d4-0004-0004-0004-000000000004', 'Repair Queue', 'OPEN', 12, 30, NOW());

-- Sample waiting tokens for City Care Clinic queue
INSERT INTO queue_tokens (id, queue_id, customer_id, token_number, status, joined_at) VALUES
('d1000001-0001-0001-0001-000000000001', 'c1000001-0001-0001-0001-000000000001', '00000000-0000-0000-0000-000000000001', 22, 'WAITING', NOW()),
('d1000001-0001-0001-0001-000000000002', 'c1000001-0001-0001-0001-000000000001', '00000000-0000-0000-0000-000000000002', 23, 'WAITING', NOW()),
('d1000001-0001-0001-0001-000000000003', 'c1000001-0001-0001-0001-000000000001', '00000000-0000-0000-0000-000000000003', 24, 'WAITING', NOW()),
('d1000001-0001-0001-0001-000000000004', 'c1000001-0001-0001-0001-000000000001', '00000000-0000-0000-0000-000000000004', 25, 'WAITING', NOW()),
('d1000001-0001-0001-0001-000000000005', 'c1000001-0001-0001-0001-000000000001', '00000000-0000-0000-0000-000000000005', 26, 'WAITING', NOW());
