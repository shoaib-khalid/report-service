##################################################
# Version v.3.1.0 | 13-December-2021
##################################################
### Code Changes:

1. Bug fixed get **DetailDailySale** endpoint 


##################################################
# Version v.3.0.9 | 03-December-2021
##################################################
### Code Changes:

1. Bug fixed get **MerchantDailyTopProducts** endpoint and pagination


##################################################
# Version v.3.0.8 | 01-December-2021
##################################################
### Code Changes:

1. Update the insertDailySales Procedure Update
 ----------------------------------------------

CREATE DEFINER=`root`@`localhost` PROCEDURE `symplified`.`insertDailySales`()
BEGIN
   DECLARE deliveryTypes varchar(50) ;
   DECLARE i int ;
   DECLARE totalType int ;

	SELECT COUNT(*) FROM ( SELECT os.deliveryType FROM symplified.`order` os GROUP BY os.deliveryType) AS i;
	  SET i = 0;
	  WHILE i  <= 3 DO
	  	IF( i = 1 ) THEN
			INSERT INTO store_daily_sale(`date`, storeId, totalOrders, amountEarned, commision,totalServiceCharge,totalDeliveryFee, totalAmount) 
	    	SELECT DATE(o.created), o.storeId, COUNT(o.created), IFNULL(SUM(o.storeShare), 0) + IFNULL(SUM(opd.deliveryQuotationAmount),0) , IFNULL(SUM(o.klCommission), 0),IFNULL(SUM(o.storeServiceCharges),0) , 
			IFNULL(SUM(opd.deliveryQuotationAmount),0) ,  IFNULL(SUM(o.total),0)
			FROM symplified.`order` o INNER JOIN symplified.order_payment_detail opd ON opd.orderId=o.id
			WHERE 
			`created` IS NOT NULL AND o.deliveryType = "SELF"
			AND storeId IS NOT NULL 
			#AND DATE(created)=DATE(NOW()) 
			AND  1<=(SELECT COUNT(*) FROM order_payment_status_update opsu WHERE opsu.status='PAID' AND opsu.orderId=o.id)
			GROUP BY DATE(created), storeId
			ORDER BY DATE(created)
			ON DUPLICATE KEY UPDATE
			storeId=storeId;		
		ELSEIF( i = 2 )THEN
			INSERT INTO store_daily_sale(`date`, storeId, totalOrders, amountEarned, commision,totalServiceCharge,totalDeliveryFee, totalAmount) 
		    SELECT DATE(o.created), o.storeId, COUNT(o.created), IFNULL(SUM(o.storeShare), 0), IFNULL(SUM(o.klCommission), 0),IFNULL(SUM(o.storeServiceCharges),0) , 
			IFNULL(SUM(opd.deliveryQuotationAmount),0) ,  IFNULL(SUM(o.total),0)
			FROM symplified.`order` o INNER JOIN symplified.order_payment_detail opd ON opd.orderId=o.id
			WHERE 
			`created` IS NOT NULL AND o.deliveryType = "ADHOC"
			AND storeId IS NOT NULL 
			#AND DATE(created)=DATE(NOW()) 
			AND  1<=(SELECT COUNT(*) FROM order_payment_status_update opsu WHERE opsu.status='PAID' AND opsu.orderId=o.id)
			GROUP BY DATE(created), storeId
			ORDER BY DATE(created)
			ON DUPLICATE KEY UPDATE
			storeId=storeId;
		ELSE
			INSERT INTO store_daily_sale(`date`, storeId, totalOrders, amountEarned, commision,totalServiceCharge,totalDeliveryFee, totalAmount) 
		    SELECT DATE(o.created), o.storeId, COUNT(o.created), IFNULL(SUM(o.storeShare), 0), IFNULL(SUM(o.klCommission), 0),IFNULL(SUM(o.storeServiceCharges),0) , 
			IFNULL(SUM(opd.deliveryQuotationAmount),0) ,  IFNULL(SUM(o.total),0)
			FROM symplified.`order` o INNER JOIN symplified.order_payment_detail opd ON opd.orderId=o.id
			WHERE 
			`created` IS NOT NULL AND o.deliveryType = "SCHEDULED"
			AND storeId IS NOT NULL 
			#AND DATE(created)=DATE(NOW()) 
			AND  1<=(SELECT COUNT(*) FROM order_payment_status_update opsu WHERE opsu.status='PAID' AND opsu.orderId=o.id)
			GROUP BY DATE(created), storeId
			ORDER BY DATE(created)
			ON DUPLICATE KEY UPDATE
			storeId=storeId;
		END IF;
	            SET i = i + 1;

      END WHILE;
END

----------------------------------------------------------------------------------------------------------------------------------------------------------------
2. Added New point for report service 
   1. Pagination 
   2. Sort by 
   3. Limit 
   4. Swagger Endpoint Name Change






